
using pulsecharge.Dtos;
using pulsecharge.Repositories;
using pulsecharge.Security;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using System.Text;   // for Encoding
using MongoDB.Bson;       // <-- Add this

namespace pulsecharge.Controllers
{
    [ApiController]
    [Route("api/v1/operator")]
    [Authorize(Policy = Policies.OperatorOnly)]
    public class OperatorController : ControllerBase
    {
        private readonly BookingRepository _bookings;
        public OperatorController(BookingRepository bookings){ _bookings = bookings; }

        [HttpPost("scan")]
        public async Task<IActionResult> Scan([FromBody] OperatorScanDto dto)
        {
            try
            {
                Console.WriteLine("Received QR: " + dto.Qr);

                // 1. Decode Base64 QR string
                string decodedQr;
                try
                {
                    decodedQr = Encoding.UTF8.GetString(Convert.FromBase64String(dto.Qr));
                    Console.WriteLine("Decoded QR: " + decodedQr);
                }
                catch (FormatException)
                {
                    Console.WriteLine("Failed to decode Base64 QR");
                    return BadRequest(new { message = "QR is not valid Base64" });
                }

                // 2. Split QR by colon
                var parts = decodedQr.Split(':', 4);
                Console.WriteLine("QR parts count: " + parts.Length);
                Console.WriteLine("QR parts: " + string.Join(", ", parts));

                // 3. Validate QR format
                if (parts.Length != 4 || parts[0] != "booking")
                {
                    Console.WriteLine("Invalid QR format detected");
                    return BadRequest(new { message = "Invalid QR format" });
                }

                // 4. Parse bookingId, stationId, startTime
                ObjectId bookingId;
                ObjectId stationId;
                DateTime startTime;

                try
                {
                    bookingId = ObjectId.Parse(parts[1]);
                    stationId = ObjectId.Parse(parts[2]);
                    startTime = DateTime.Parse(parts[3]);
                    Console.WriteLine($"Parsed bookingId: {bookingId}, stationId: {stationId}, startTime: {startTime}");
                }
                catch (Exception ex)
                {
                    Console.WriteLine("Error parsing QR data: " + ex.Message);
                    return BadRequest(new { message = "Invalid QR data format", error = ex.Message });
                }

                // 5. Find booking in DB
                var b = await _bookings.Collection.Find(x =>
                    x.Id == bookingId &&
                    x.StationId == stationId &&
                    x.StartTime == startTime &&
                    x.Status == "approved"
                ).FirstOrDefaultAsync();

                if (b == null)
                {
                    Console.WriteLine("Booking not found or not approved");
                    return NotFound(new { message = "Booking not found or not approved" });
                }

                // 6. Return booking details
                Console.WriteLine("Booking found: " + b.Id);
                return Ok(new
                {
                    _id = b.Id.ToString(),
                    b.StationId,
                    b.StartTime,
                    b.EndTime,
                    b.Status,
                    b.SlotNumber
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine("Server exception: " + ex.Message);
                return StatusCode(500, new { message = "Server error", error = ex.Message });
            }
        }


        [HttpPost("bookings/{id}:confirm-arrival")]
        public async Task<IActionResult> ConfirmArrival(string id)
        {
            var b = await _bookings.FindAsync(MongoDB.Bson.ObjectId.Parse(id)); if (b==null) return NotFound();
            if (b.Status != "approved") return Conflict(new { code="E_STATE", message="Not approved" });
            b.Status = "in_progress"; b.UpdatedAt = DateTime.UtcNow;
            await _bookings.ReplaceAsync(b);
            return Ok(new { _id=b.Id.ToString(), b.Status });
        }

        [HttpPost("bookings/{id}:finalize")]
        public async Task<IActionResult> Finalize(string id, [FromBody] pulsecharge.Dtos.OperatorFinalizeDto dto)
        {
            var b = await _bookings.FindAsync(MongoDB.Bson.ObjectId.Parse(id)); if (b==null) return NotFound();
            b.Status = "completed"; b.FinalizedAt = DateTime.UtcNow; b.EnergyKWh=dto.EnergyKWh; b.Notes=dto.Notes;
            await _bookings.ReplaceAsync(b);
            return Ok(new { _id=b.Id.ToString(), b.Status, b.FinalizedAt });
        }
    }
}
