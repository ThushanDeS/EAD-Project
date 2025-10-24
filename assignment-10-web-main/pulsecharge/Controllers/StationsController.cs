using pulsecharge.Dtos;
using pulsecharge.Models;
using pulsecharge.Repositories;
using pulsecharge.Security;
using pulsecharge.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Bson;
using MongoDB.Driver;
using System.Security.Claims;

namespace pulsecharge.Controllers
{
    [ApiController]
    [Route("api/v1/stations")]
    public class StationsController : ControllerBase
    {
        private readonly StationRepository _stations;
        private readonly BookingRepository _bookings;
        private readonly AvailabilityService _availability;
        public StationsController(StationRepository s, BookingRepository b, AvailabilityService a) { _stations = s; _bookings = b; _availability = a; }

        [HttpPost]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> Create([FromBody] CreateStationDto dto)
        {
            var s = new Station
            {
                Id = ObjectId.GenerateNewId(),
                Name = dto.Name,
                Type = dto.Type,
                SlotCount = dto.SlotCount,
                Location = dto.Location,
                Hours = dto.Hours,
                OperatorIds = dto.OperatorIds?.Select(ObjectId.Parse).ToList() ?? new List<ObjectId>()
            };
            await _stations.CreateAsync(s);
            return Ok(new { _id = s.Id.ToString() });
        }

        [HttpPatch("{id}")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> Patch(string id, [FromBody] UpdateStationDto dto)
        {
            if (!ObjectId.TryParse(id, out var oid)) return BadRequest();
            var s = await _stations.FindAsync(oid); if (s == null) return NotFound();
            if (dto.Name != null) s.Name = dto.Name;
            if (dto.Type != null) s.Type = dto.Type;
            if (dto.SlotCount.HasValue) s.SlotCount = dto.SlotCount.Value;
            if (dto.Location != null) s.Location = dto.Location;
            if (dto.Hours != null) s.Hours = dto.Hours;
            if (dto.OperatorIds != null) s.OperatorIds = dto.OperatorIds.Select(ObjectId.Parse).ToList();
            s.UpdatedAt = DateTime.UtcNow;
            await _stations.ReplaceAsync(s);
            return Ok();
        }

        [HttpPost("{id}:assign-operators")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> AssignOperators(string id, [FromBody] AssignOperatorsDto dto)
        {
            if (!ObjectId.TryParse(id, out var oid)) return BadRequest();
            var s = await _stations.FindAsync(oid); if (s == null) return NotFound();
            s.OperatorIds = dto.OperatorIds.Select(ObjectId.Parse).ToList();
            s.UpdatedAt = DateTime.UtcNow;
            await _stations.ReplaceAsync(s);
            return Ok();
        }

        [HttpGet]
        public async Task<IActionResult> Query([FromQuery] bool? active, [FromQuery] string? type)
        {
            var items = await _stations.Query(active, type).ToListAsync();
            return Ok(items.Select(s => new {
                _id = s.Id.ToString(),
                s.Name,
                s.Type,
                s.SlotCount,
                s.Active,
                lat = s.Location.Lat,
                lng = s.Location.Lng,
                address = s.Location.Address ?? "",
                operatorId = s.OperatorIds.Select(o => o.ToString()).FirstOrDefault()
            }));
        }

        [HttpGet("{id}/availability")]
        public async Task<IActionResult> Availability(string id, [FromQuery] string date)
        {
            if (!DateTime.TryParse(date, out var d)) return BadRequest("Bad date");
            var res = await _availability.GetAvailabilityAsync(id, d.ToUniversalTime());
            if (res == null) return NotFound();
            return Ok(res);
        }

        [HttpPost("{id}:deactivate")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> Deactivate(string id)
        {
            if (!ObjectId.TryParse(id, out var oid)) return BadRequest();
            var s = await _stations.FindAsync(oid); if (s == null) return NotFound();

            var filter = Builders<Booking>.Filter.And(
                Builders<Booking>.Filter.Eq(b => b.StationId, oid),
                Builders<Booking>.Filter.Or(
                    Builders<Booking>.Filter.In(b => b.Status, new[] { "pending", "approved", "in_progress" }),
                    Builders<Booking>.Filter.And(
                        Builders<Booking>.Filter.Eq(b => b.Status, "completed"),
                        Builders<Booking>.Filter.Gte(b => b.StartTime, DateTime.UtcNow)
                    )
                )
            );
            var activeFuture = await _bookings.Collection.Find(filter).AnyAsync();
            if (activeFuture) return Conflict(new { code = "E_STATION_DEACT_BLOCKED", message = "Active/future bookings exist" });

            s.Active = false; s.UpdatedAt = DateTime.UtcNow;
            await _stations.ReplaceAsync(s);
            return Ok();
        }

        [HttpPost("{id}:reactivate")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> Reactivate(string id)
        {
            if (!ObjectId.TryParse(id, out var oid)) return BadRequest();
            var s = await _stations.FindAsync(oid); if (s == null) return NotFound();

            s.Active = true; s.UpdatedAt = DateTime.UtcNow;
            await _stations.ReplaceAsync(s);
            return Ok();
        }

        [HttpGet("me")]
        [Authorize(Policy = Policies.OperatorOnly)]
        public async Task<IActionResult> GetMyStation()
        {
            Console.WriteLine("DEBUG --- /api/v1/stations/me hit ---");

            // Extract operator ID from token
            var operatorSub = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(operatorSub) || !ObjectId.TryParse(operatorSub, out var operatorId))
            {
                return Unauthorized(new { message = "Invalid token" });
            }

            // Find station assigned to this operator
            var station = await _stations.FindByOperatorIdAsync(operatorId);
            if (station == null)
            {
                return StatusCode(403, new { message = "Operator not assigned to any station" });
            }

            // Return station info
            return Ok(new
            {
                _id = station.Id.ToString(),
                station.Name,
                station.Type,
                station.SlotCount,
                station.Active,
                lat = station.Location.Lat,
                lng = station.Location.Lng,
                station.Hours
            });
        }
    }
}