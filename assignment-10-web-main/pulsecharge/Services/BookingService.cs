
// BookingService.cs
// Business logic for bookings: creating, updating, approving, rejecting and cancelling bookings.
// This service enforces validation rules (time windows, slot bounds, overlapping checks) and
// handles QR code generation via QrCodeService.
using pulsecharge.Models;
using pulsecharge.Repositories;
using pulsecharge.Security;
using MongoDB.Bson;

namespace pulsecharge.Services
{
    public class BookingService
    {
        private readonly BookingRepository _bookings;
        private readonly StationRepository _stations;
        private readonly UserRepository _users;
        private readonly QrCodeService _qrCodeService;
        
        public BookingService(BookingRepository b, StationRepository s, UserRepository u, QrCodeService q) 
        { 
            _bookings = b; 
            _stations = s; 
            _users = u; 
            _qrCodeService = q;
        }

        // Rounds a DateTime down to the hour and ensures it's UTC. This project aligns bookings to full hours.
        private static DateTime AlignHourUtc(DateTime t) => new DateTime(t.Year, t.Month, t.Day, t.Hour, 0, 0, DateTimeKind.Utc);

        // Create a new booking after performing validations. Returns either the created booking and QR
        // data, or an error string code that callers map to user-friendly messages.
        public async Task<(Booking created, string? code, string? error)> CreateAsync(ObjectId ownerId, string createdBy, string stationId, DateTime start, int slotNumber, DateTime? endTime = null)
        {
            // Validate station id
            if (!ObjectId.TryParse(stationId, out var sid)) return (null!, null, "E_BAD_STATION");
            var station = await _stations.FindAsync(sid);
            if (station == null || !station.Active) return (null!, null, "E_STATION_INACTIVE");

            // Validate owner
            var owner = await _users.FindByIdAsync(ownerId);
            if (owner == null || owner.Status != "active") return (null!, null, "E_OWNER_INACTIVE");

            var utcNow = DateTime.UtcNow;
            var aligned = AlignHourUtc(start);
            // Booking window: aligned start must be between now and 7 days from now
            if (aligned < utcNow || aligned > utcNow.AddDays(7)) return (null!, null, "E_WINDOW_7D");

            // Calculate end time: use provided endTime or default to 1 hour after start
            var calculatedEndTime = endTime ?? aligned.AddHours(1);

            // Validate end time
            if (calculatedEndTime <= aligned) return (null!, null, "E_INVALID_END_TIME");
            if (calculatedEndTime > utcNow.AddDays(7)) return (null!, null, "E_END_TIME_TOO_FAR");

            // Validate slot number against station slot count
            if (slotNumber < 1 || slotNumber > station.SlotCount) return (null!, null, "E_INVALID_SLOT");

            // Check for overlapping bookings in the same slot
            var hasOverlap = await _bookings.HasOverlappingBooking(sid, slotNumber, aligned, calculatedEndTime);
            if (hasOverlap) return (null!, null, "E_TIME_OVERLAP");

            // Set status: backoffice creates pre-approved bookings
            var status = createdBy == Roles.Backoffice ? "approved" : "pending";

            var booking = new Booking
            {
                Id = ObjectId.GenerateNewId(),
                StationId = sid,
                SlotNumber = slotNumber,
                OwnerId = ownerId,
                StartTime = aligned,
                EndTime = calculatedEndTime,
                Status = status,
                CreatedBy = createdBy
            };
            
            // Generate QR code payload (used by apps and operators); keep a Base64 PNG image for convenience
            var qrPayload = $"booking:{booking.Id}:{sid}:{aligned:o}";
            var (qrText, qrImageBase64) = _qrCodeService.GenerateQrCode(qrPayload);
            booking.QrCode = qrText;
            booking.QrImageBase64 = qrImageBase64;
            
            await _bookings.CreateAsync(booking);
            return (booking, booking.QrCode, null);
        }

        // Update booking (partial). Returns updated booking or an error code. Respects 12h cutoff rule.
        public async Task<(Booking? updated, string? error)> UpdateAsync(ObjectId bookingId, DateTime? newStart, int? newSlot, DateTime? newEndTime = null)
        {
            var b = await _bookings.FindAsync(bookingId);
            if (b == null) return (null, "E_NOT_FOUND");
            if (DateTime.UtcNow > b.StartTime.AddHours(-12)) return (null, "E_CUTOFF_12H");

            // Get station details for validation
            var station = await _stations.FindAsync(b.StationId);
            if (station == null || !station.Active) return (null, "E_STATION_INACTIVE");

            var originalStartTime = b.StartTime;
            var originalEndTime = b.EndTime;
            var originalSlotNumber = b.SlotNumber;

            var utcNow = DateTime.UtcNow;

            if (newStart != null)
            {
                var aligned = AlignHourUtc(newStart.Value);
                
                // Validate booking window (within 7 days)
                if (aligned < utcNow || aligned > utcNow.AddDays(7)) return (null, "E_WINDOW_7D");
                
                b.StartTime = aligned;
                // Only update end time if not explicitly provided
                if (newEndTime == null)
                {
                    b.EndTime = aligned.AddHours(1);
                }
            }

            if (newEndTime != null)
            {
                // Validate that end time is after start time
                if (newEndTime <= b.StartTime) return (null, "E_INVALID_END_TIME");
                
                // Validate end time is within 7-day window
                if (newEndTime > utcNow.AddDays(7)) return (null, "E_END_TIME_TOO_FAR");
                
                b.EndTime = newEndTime.Value;
            }

            if (newSlot != null) 
            {
                // Validate slot number
                if (newSlot < 1 || newSlot > station.SlotCount) return (null, "E_INVALID_SLOT");
                b.SlotNumber = newSlot.Value;
            }

            // Check for overlapping bookings if time or slot changed
            if (newStart != null || newEndTime != null || newSlot != null)
            {
                var hasOverlap = await _bookings.HasOverlappingBooking(b.StationId, b.SlotNumber, b.StartTime, b.EndTime, bookingId);
                if (hasOverlap)
                {
                    // Restore original values
                    b.StartTime = originalStartTime;
                    b.EndTime = originalEndTime;
                    b.SlotNumber = originalSlotNumber;
                    return (null, "E_TIME_OVERLAP");
                }
            }

            // Set status to pending after any update - requires re-approval
            b.Status = "pending";
            b.UpdatedAt = DateTime.UtcNow;
            await _bookings.ReplaceAsync(b);
            return (b, null);
        }

        // Cancel booking if cutoff rules allow
        public async Task<(Booking? updated, string? error)> CancelAsync(ObjectId bookingId)
        {
            var b = await _bookings.FindAsync(bookingId);
            if (b == null) return (null, "E_NOT_FOUND");
            if (DateTime.UtcNow > b.StartTime.AddHours(-12)) return (null, "E_CUTOFF_12H");
            b.Status = "cancelled"; b.UpdatedAt = DateTime.UtcNow;
            await _bookings.ReplaceAsync(b);
            return (b, null);
        }

        // Approve a pending booking (operator action)
        public async Task<(Booking? updated, string? error)> ApproveAsync(ObjectId bookingId)
        {
            var b = await _bookings.FindAsync(bookingId);
            if (b == null) return (null, "E_NOT_FOUND");
            if (b.Status != "pending") return (null, "E_NOT_PENDING");
            if (DateTime.UtcNow > b.StartTime.AddHours(-12)) return (null, "E_CUTOFF_12H");
            b.Status = "approved"; b.UpdatedAt = DateTime.UtcNow;
            await _bookings.ReplaceAsync(b);
            return (b, null);
        }

        // Reject a pending booking (operator action)
        public async Task<(Booking? updated, string? error)> RejectAsync(ObjectId bookingId)
        {
            var b = await _bookings.FindAsync(bookingId);
            if (b == null) return (null, "E_NOT_FOUND");
            if (b.Status != "pending") return (null, "E_NOT_PENDING");
            if (DateTime.UtcNow > b.StartTime.AddHours(-12)) return (null, "E_CUTOFF_12H");
            b.Status = "rejected"; b.UpdatedAt = DateTime.UtcNow;
            await _bookings.ReplaceAsync(b);
            return (b, null);
        }
        
        // Helper to fetch bookings for a station by string id
        public async Task<List<Booking>> GetBookingsForStationAsync(string stationId)
        {
            if (!ObjectId.TryParse(stationId, out var sid))
                throw new Exception("Invalid stationId");

            return await _bookings.ForStationAsync(sid);
        }
    }
}
