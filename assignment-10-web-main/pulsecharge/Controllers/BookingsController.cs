using pulsecharge.Dtos;
using pulsecharge.Repositories;
using pulsecharge.Security;
using pulsecharge.Services;
using pulsecharge.Models;
using pulsecharge.Extensions;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Bson;
using MongoDB.Driver;
using System.Security.Claims;

namespace pulsecharge.Controllers
{
    [ApiController]
    [Route("api/v1")]
    public class BookingsController : ControllerBase
    {
        private readonly StationRepository _stationRepo;
        private readonly BookingService _bookingService;
        private readonly BookingRepository _bookingRepo;
        private readonly UserRepository _userRepo;
        private readonly QrCodeService _qrService;

        public BookingsController(StationRepository stationRepo, BookingService bookingService, BookingRepository bookingRepo, UserRepository userRepo, QrCodeService qrService)
        {
            _stationRepo = stationRepo;
            _bookingService = bookingService;
            _bookingRepo = bookingRepo;
            _userRepo = userRepo;
            _qrService = qrService;
        }

        [HttpPost("bookings")]
        [Authorize]
        public async Task<IActionResult> Create([FromBody] CreateBookingDto dto)
        {
            var role = User.FindFirst(System.Security.Claims.ClaimTypes.Role)?.Value;

            if (string.IsNullOrEmpty(role))
                return Unauthorized(new { message = "Invalid or missing token" });

            var ownerIdHeader = Request.Headers["X-OwnerId"].FirstOrDefault();
            if (string.IsNullOrEmpty(ownerIdHeader) || !ObjectId.TryParse(ownerIdHeader, out var ownerId))
            {
                return BadRequest(new { message = "Missing or invalid X-OwnerId header" });
            }

            var endTime = dto.EndTime?.ToUniversalTime();
            var (created, qr, err) = await _bookingService.CreateAsync(ownerId, role, dto.StationId, dto.StartTime.ToUniversalTime(), dto.SlotNumber, endTime);
            if (err != null) return Conflict(new { code = err, message = err });

            return Created($"/api/v1/bookings/{created.Id}", new
            {
                _id = created.Id.ToString(),
                status = created.Status,
                qrCode = created.QrCode,
                qrImageBase64 = created.QrImageBase64,
                qrImageContentType = created.QrImageContentType,
                startTime = created.StartTime,
                endTime = created.EndTime,
                slotNumber = created.SlotNumber
            });
        }

        [HttpPost("bookings/backoffice-create")]
        [Authorize(Roles = Roles.Backoffice)]
        public async Task<IActionResult> BackofficeCreate([FromBody] BackofficeCreateBookingDto dto)
        {
            if (!ObjectId.TryParse(dto.OwnerId, out var ownerId))
                return BadRequest(new { message = "Invalid OwnerId format" });

            var role = User.FindFirst(System.Security.Claims.ClaimTypes.Role)?.Value;

            var (created, qr, err) = await _bookingService.CreateAsync(ownerId, role, dto.StationId, dto.StartTime.ToUniversalTime(), dto.SlotNumber, dto.EndTime.ToUniversalTime());
            if (err != null) return Conflict(new { code = err, message = err });

            return Created($"/api/v1/bookings/{created.Id}", new
            {
                _id = created.Id.ToString(),
                status = created.Status,
                qrCode = created.QrCode,
                startTime = created.StartTime,
                endTime = created.EndTime,
                slotNumber = created.SlotNumber
            });
        }

        [HttpGet("bookings/backoffice")]
        [Authorize(Roles = Roles.Backoffice)]
        public async Task<IActionResult> GetAllForBackoffice()
        {
            var bookings = await _bookingRepo.GetAllAsync();
            var bookingDetails = new List<object>();

            foreach (var booking in bookings)
            {
                var owner = await _userRepo.FindByIdAsync(booking.OwnerId);
                var station = await _stationRepo.FindAsync(booking.StationId);

                bookingDetails.Add(new
                {
                    _id = booking.Id.ToString(),
                    evNic = owner?.Nic,
                    stationName = station?.Name,
                    startUtc = booking.StartTime,
                    endUtc = booking.EndTime,
                    status = booking.Status
                });
            }

            return Ok(bookingDetails);
        }

        [HttpGet("bookings/recent")]
        [Authorize(Roles = Roles.Backoffice)]
        public async Task<IActionResult> GetRecentBookings()
        {
            var threeDaysAgo = DateTime.UtcNow.AddDays(-3);
            var filter = Builders<Booking>.Filter.Gte(b => b.StartTime, threeDaysAgo);
            var bookings = await _bookingRepo.Collection.Find(filter).SortByDescending(b => b.StartTime).Limit(5).ToListAsync();

            var bookingDetails = new List<object>();

            foreach (var booking in bookings)
            {
                var owner = await _userRepo.FindByIdAsync(booking.OwnerId);
                var station = await _stationRepo.FindAsync(booking.StationId);

                bookingDetails.Add(new
                {
                    _id = booking.Id.ToString(),
                    evNic = owner?.Nic,
                    stationName = station?.Name,
                    startUtc = booking.StartTime,
                    status = booking.Status
                });
            }

            return Ok(bookingDetails);
        }

        [HttpGet("bookings/dashboard/pending-count")]
        [Authorize(Roles = Roles.Backoffice)]
        public async Task<IActionResult> GetPendingCount()
        {
            var filter = Builders<Booking>.Filter.Regex(b => b.Status, new BsonRegularExpression("^pending$", "i"));
            var count = await _bookingRepo.Collection.CountDocumentsAsync(filter);
            return Ok(new { count });
        }

        [HttpGet("bookings/dashboard/future-approved-count")]
        [Authorize(Roles = Roles.Backoffice)]
        public async Task<IActionResult> GetFutureApprovedCount()
        {
            var filter = Builders<Booking>.Filter.And(
                Builders<Booking>.Filter.Regex(b => b.Status, new BsonRegularExpression("^approved$", "i")),
                Builders<Booking>.Filter.Gte(b => b.StartTime, DateTime.UtcNow)
            );
            var count = await _bookingRepo.Collection.CountDocumentsAsync(filter);
            return Ok(new { count });
        }

        [HttpPatch("bookings/{id}")]
        [Authorize]
        public async Task<IActionResult> Patch(string id, [FromBody] UpdateBookingDto dto)
        {
            if (!ObjectId.TryParse(id, out var oid)) return BadRequest();
            var endTime = dto.EndTime?.ToUniversalTime();
            var (b, err) = await _bookingService.UpdateAsync(oid, dto.StartTime?.ToUniversalTime(), dto.SlotNumber, endTime);
            if (err != null)
            {
                var message = err switch
                {
                    "E_NOT_FOUND" => "Booking not found",
                    "E_CUTOFF_12H" => "Changes allowed only ≥12h before start",
                    "E_STATION_INACTIVE" => "Station is inactive",
                    "E_WINDOW_7D" => "Booking must be within 7 days",
                    "E_INVALID_END_TIME" => "End time must be after start time",
                    "E_END_TIME_TOO_FAR" => "End time cannot be more than 7 days from now",
                    "E_INVALID_SLOT" => "Invalid slot number for this station",
                    "E_TIME_OVERLAP" => "Time slot is already booked",
                    _ => "An error occurred"
                };
                return Conflict(new { code = err, message = message });
            }
            return Ok(new { _id = b!.Id.ToString(), b.Status, b.StartTime, b.EndTime, b.SlotNumber });
        }

        [HttpPost("bookings/{id}:cancel")]
        [Authorize]
        public async Task<IActionResult> Cancel(string id)
        {
            if (!ObjectId.TryParse(id, out var oid)) return BadRequest();
            var (b, err) = await _bookingService.CancelAsync(oid);
            if (err != null) return Conflict(new { code = err, message = "Cancel allowed only ≥12h before start" });
            return Ok(new { _id = b!.Id.ToString(), b.Status });
        }

        [HttpPost("bookings/{id}/approve")]
        [Authorize(Policy = Policies.OperatorOnly)]
        public async Task<IActionResult> Approve(string id)
        {
            if (!ObjectId.TryParse(id, out var oid)) return BadRequest();
            var (b, err) = await _bookingService.ApproveAsync(oid);
            if (err != null)
            {
                var message = err switch
                {
                    "E_NOT_FOUND" => "Booking not found",
                    "E_NOT_PENDING" => "Booking is not in pending status",
                    "E_CUTOFF_12H" => "Cannot approve booking less than 12 hours before start time",
                    _ => "An error occurred"
                };
                return Conflict(new { code = err, message = message });
            }
            return Ok(new { _id = b!.Id.ToString(), b.Status });
        }

        [HttpPost("bookings/{id}:reject")]
        [Authorize(Policy = Policies.OperatorOnly)]
        public async Task<IActionResult> Reject(string id)
        {
            if (!ObjectId.TryParse(id, out var oid)) return BadRequest();
            var (b, err) = await _bookingService.RejectAsync(oid);
            if (err != null)
            {
                var message = err switch
                {
                    "E_NOT_FOUND" => "Booking not found",
                    "E_NOT_PENDING" => "Booking is not in pending status",
                    "E_CUTOFF_12H" => "Cannot reject booking less than 12 hours before start time",
                    _ => "An error occurred"
                };
                return Conflict(new { code = err, message = message });
            }
            return Ok(new { _id = b!.Id.ToString(), b.Status });
        }

        [HttpGet("bookings/mine")]
        [Authorize(Policy = Policies.OwnerOnly)]
        public async Task<IActionResult> Mine([FromQuery] string? status, [FromQuery] bool? future)
        {
            var sub = User.FindFirst("sub")?.Value ?? User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            if (string.IsNullOrEmpty(sub) || !ObjectId.TryParse(sub, out var ownerId))
                return Unauthorized(new { message = "Invalid token: missing or bad sub claim" });

            var role = User.FindFirst("role")?.Value ?? User.FindFirst(ClaimTypes.Role)?.Value ?? User.FindFirst("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")?.Value;

            if (role != Roles.EvOwner)
                return Forbid();

            var filter = Builders<Booking>.Filter.Eq(b => b.OwnerId, ownerId);

            if (!string.IsNullOrEmpty(status))
                filter &= Builders<Booking>.Filter.In(b => b.Status, status.Split(','));

            if (future == true)
                filter &= Builders<Booking>.Filter.Gte(b => b.StartTime, DateTime.UtcNow);

            var list = await _bookingRepo.Collection.Find(filter).ToListAsync();

            return Ok(list.Select(b => new
            {
                _id = b.Id.ToString(),
                stationId = b.StationId.ToString(),
                slotNumber = b.SlotNumber,
                ownerId = b.OwnerId.ToString(),
                startTime = b.StartTime,
                endTime = b.EndTime,
                status = b.Status,
                qrCode = b.QrCode,
                qrImageBase64 = b.QrImageBase64,
                qrImageContentType = b.QrImageContentType,
                createdBy = b.CreatedBy,
                createdAt = b.CreatedAt,
                updatedAt = b.UpdatedAt,
                finalizedBy = b.FinalizedBy?.ToString(),
                finalizedAt = b.FinalizedAt,
                energyKWh = b.EnergyKWh,
                notes = b.Notes
            }));
        }

        [HttpGet("stations/bookings")]
        [Authorize(Policy = Policies.OperatorOnly)]
        public async Task<IActionResult> ForStation()
        {
            var operatorSub = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            if (string.IsNullOrEmpty(operatorSub) || !ObjectId.TryParse(operatorSub, out var operatorId))
            {
                return Unauthorized(new { message = "Invalid token" });
            }

            var station = await _stationRepo.FindByOperatorIdAsync(operatorId);
            if (station == null)
            {
                return StatusCode(403, new { message = "Operator not assigned to any station" });
            }

            var list = await _bookingService.GetBookingsForStationAsync(station.Id.ToString());

            var bookingDetails = new List<object>();

            foreach (var booking in list)
            {
                var owner = await _userRepo.FindByIdAsync(booking.OwnerId);

                bookingDetails.Add(new
                {
                    _id = booking.Id.ToString(),
                    stationId = booking.StationId.ToString(),
                    slotNumber = booking.SlotNumber,
                    ownerId = booking.OwnerId.ToString(),
                    ownerName = owner?.Name ?? "Unknown",
                    ownerNic = owner?.Nic,
                    startTime = booking.StartTime,
                    endTime = booking.EndTime,
                    status = booking.Status,
                    qrCode = booking.QrCode,
                    qrImageBase64 = booking.QrImageBase64,
                    qrImageContentType = booking.QrImageContentType,
                    createdBy = booking.CreatedBy,
                    createdAt = booking.CreatedAt,
                    updatedAt = booking.UpdatedAt,
                    finalizedBy = booking.FinalizedBy?.ToString(),
                    finalizedAt = booking.FinalizedAt,
                    energyKWh = booking.EnergyKWh,
                    notes = booking.Notes
                });
            }

            return Ok(bookingDetails);
        }

        // Get QR code details for a specific booking
        [HttpGet("bookings/{id}/qr")]
        [Authorize]
        public async Task<IActionResult> GetBookingQr(string id)
        {
            if (!ObjectId.TryParse(id, out var oid)) 
                return BadRequest(new { message = "Invalid booking ID" });

            var booking = await _bookingRepo.FindAsync(oid);
            if (booking == null) 
                return NotFound(new { message = "Booking not found" });

            return Ok(booking.ToQrResponseDto());
        }

        // Get QR code image as file (for direct image display)
        [HttpGet("bookings/{id}/qr/image")]
        [Authorize]
        public async Task<IActionResult> GetBookingQrImage(string id)
        {
            if (!ObjectId.TryParse(id, out var oid)) 
                return BadRequest(new { message = "Invalid booking ID" });

            var booking = await _bookingRepo.FindAsync(oid);
            if (booking == null) 
                return NotFound(new { message = "Booking not found" });

            if (string.IsNullOrEmpty(booking.QrImageBase64))
                return NotFound(new { message = "QR image not available" });

            try
            {
                var imageBytes = Convert.FromBase64String(booking.QrImageBase64);
                return File(imageBytes, booking.QrImageContentType ?? "image/png");
            }
            catch (Exception)
            {
                return BadRequest(new { message = "Invalid QR image data" });
            }
        }

        // Get detailed booking information with QR code
        [HttpGet("bookings/{id}")]
        [Authorize]
        public async Task<IActionResult> GetBookingDetails(string id)
        {
            if (!ObjectId.TryParse(id, out var oid)) 
                return BadRequest(new { message = "Invalid booking ID" });

            var booking = await _bookingRepo.FindAsync(oid);
            if (booking == null) 
                return NotFound(new { message = "Booking not found" });

            return Ok(booking.ToResponseDto());
        }

        // Test endpoint to decode QR code for verification
        [HttpPost("bookings/qr/decode")]
        [Authorize]
        public IActionResult DecodeQrCode([FromBody] DecodeQrCodeDto dto)
        {
            try
            {
                var decodedPayload = _qrService.DecodeQrCode(dto.QrCode);
                return Ok(new { 
                    success = true, 
                    originalQrCode = dto.QrCode,
                    decodedPayload = decodedPayload 
                });
            }
            catch (Exception ex)
            {
                return BadRequest(new { 
                    success = false, 
                    message = ex.Message 
                });
            }
        }
    }

    public record DecodeQrCodeDto(string QrCode);
}