
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

        /// <summary>
        /// Aligns a DateTime to the top of the hour in UTC.
        /// The system schedules bookings in hourly blocks (e.g. 10:00, 11:00) so inputs are normalized here.
        /// </summary>
        private static DateTime AlignHourUtc(DateTime t) => new DateTime(t.Year, t.Month, t.Day, t.Hour, 0, 0, DateTimeKind.Utc);

        /// <summary>
        /// Creates a booking for the given station and owner.
        /// Returns a tuple: (created booking, qr payload text, error code).
        /// Error codes are application-specific strings (e.g. "E_BAD_STATION").
        /// </summary>
        public async Task<(Booking created, string? code, string? error)> CreateAsync(ObjectId ownerId, string createdBy, string stationId, DateTime start, int slotNumber, DateTime? endTime = null)
        {
            // Validate station id and status
            if (!ObjectId.TryParse(stationId, out var sid)) return (null!, null, "E_BAD_STATION");
            var station = await _stations.FindAsync(sid);
            if (station == null || !station.Active) return (null!, null, "E_STATION_INACTIVE");

            // Validate owner
            var owner = await _users.FindByIdAsync(ownerId);
            if (owner == null || owner.Status != "active") return (null!, null, "E_OWNER_INACTIVE");

            var utcNow = DateTime.UtcNow;
            // Align requested start to top of hour (system uses hourly blocks)
            var aligned = AlignHourUtc(start);
            // Enforce booking window: cannot book in the past and cannot book more than 7 days ahead
            if (aligned < utcNow || aligned > utcNow.AddDays(7)) return (null!, null, "E_WINDOW_7D");

            // Calculate end time: use provided endTime or default to 1 hour after start
            var calculatedEndTime = endTime ?? aligned.AddHours(1);

            // Validate end time (must be after start and within 7-day window)
            if (calculatedEndTime <= aligned) return (null!, null, "E_INVALID_END_TIME");
            if (calculatedEndTime > utcNow.AddDays(7)) return (null!, null, "E_END_TIME_TOO_FAR");

            // Validate slot number against station capacity
            if (slotNumber < 1 || slotNumber > station.SlotCount) return (null!, null, "E_INVALID_SLOT");

            // Check for overlapping bookings in the same slot
            var hasOverlap = await _bookings.HasOverlappingBooking(sid, slotNumber, aligned, calculatedEndTime);
            if (hasOverlap) return (null!, null, "E_TIME_OVERLAP");

            // Determine initial status: backoffice-created bookings are automatically approved
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
            
            // Generate QR code payload and base64 image for verification/UX. QR contains booking id, station id and time.
            var qrPayload = $"booking:{booking.Id}:{sid}:{aligned:o}";
            var (qrText, qrImageBase64) = _qrCodeService.GenerateQrCode(qrPayload);
            booking.QrCode = qrText;
            booking.QrImageBase64 = qrImageBase64;
            
            await _bookings.CreateAsync(booking);
            return (booking, booking.QrCode, null);
        }

        /// <summary>
        /// Update an existing booking. Returns the updated booking or an error code string.
        /// Business rules enforced here:
        /// - Updates are not allowed within 12 hours of the start time (cutoff)
        /// - Start times are aligned to the hour and must remain within the 7-day booking window
        /// - End times must be after start and within window
        /// - Slot numbers must be valid for the station
        /// - Overlaps with other pending/approved bookings are rejected
        /// - After any successful update, the booking becomes "pending" and requires approval
        /// </summary>
        public async Task<(Booking? updated, string? error)> UpdateAsync(ObjectId bookingId, DateTime? newStart, int? newSlot, DateTime? newEndTime = null)
        {
            var b = await _bookings.FindAsync(bookingId);
            if (b == null) return (null, "E_NOT_FOUND");
            // Prevent last-minute changes: cutoff 12 hours before start
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

        /// <summary>
        /// Cancel a booking if it exists and is not inside the 12-hour cutoff window.
        /// </summary>
        public async Task<(Booking? updated, string? error)> CancelAsync(ObjectId bookingId)
        {
            var b = await _bookings.FindAsync(bookingId);
            if (b == null) return (null, "E_NOT_FOUND");
            if (DateTime.UtcNow > b.StartTime.AddHours(-12)) return (null, "E_CUTOFF_12H");
            b.Status = "cancelled"; b.UpdatedAt = DateTime.UtcNow;
            await _bookings.ReplaceAsync(b);
            return (b, null);
        }

        /// <summary>
        /// Approve a pending booking. Operators/backoffice should call this.
        /// Ensures the booking is pending and not inside the cutoff window.
        /// </summary>
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

        /// <summary>
        /// Reject a pending booking. Similar checks to ApproveAsync.
        /// </summary>
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
        
        /// <summary>
        /// Convenience helper to return bookings for a station given its string id.
        /// Throws if the station id is not a valid ObjectId.
        /// </summary>
        public async Task<List<Booking>> GetBookingsForStationAsync(string stationId)
        {
            if (!ObjectId.TryParse(stationId, out var sid))
                throw new Exception("Invalid stationId");

            return await _bookings.ForStationAsync(sid);
        }
    }
}
