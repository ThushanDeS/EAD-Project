using pulsecharge.Repositories;
using pulsecharge.Security;
using pulsecharge.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Bson;
using MongoDB.Driver;
using pulsecharge.Dtos;
using System.Security.Claims;

namespace pulsecharge.Controllers
{
    [ApiController]
    [Route("api/v1")]
    public class UsersController : ControllerBase
    {
        private readonly UserRepository _users;
        public UsersController(UserRepository users) { _users = users; }

        [HttpPost("users/operators")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> CreateOperator([FromBody] CreateOperatorDto dto)
        {
            if (await _users.FindByEmailAsync(dto.Email) != null)
            {
                return Conflict(new { message = "An operator with this email already exists." });
            }

            if (await _users.FindByNicAsync(dto.Nic) != null)
            {
                return Conflict(new { message = "An operator with this NIC already exists." });
            }

            var u = new User
            {
                Id = ObjectId.GenerateNewId(),
                Role = Roles.StationOperator,
                Nic = dto.Nic,
                Email = dto.Email,
                Name = dto.Name,
                Phone = dto.Phone,
                PasswordHash = Services.AuthService.HashPassword(dto.Password)
            };
            await _users.CreateAsync(u);
            return Ok(new { _id = u.Id.ToString() });
        }

        [HttpDelete("users/operators/{id}")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> DeleteOperator(string id)
        {
            if (!ObjectId.TryParse(id, out var oid)) return BadRequest();
            await _users.DeleteAsync(oid);
            return NoContent();
        }

        [HttpPatch("users/operators/{operatorId}")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> PatchOperator(string operatorId, [FromBody] UpdateOperatorDto dto)
        {
            if (!ObjectId.TryParse(operatorId, out var oid)) return BadRequest();
            var u = await _users.FindByIdAsync(oid); if (u == null) return NotFound();

            u.Name = dto.Name;
            u.Phone = dto.Phone;
            u.Email = dto.Email;
            u.Nic = dto.Nic;
            u.UpdatedAt = DateTime.UtcNow;

            await _users.ReplaceAsync(u);
            return Ok(new { _id = u.Id.ToString(), u.Name, u.Phone, u.Email, u.Nic, u.Status });
        }

        [HttpPost("users/operators/{operatorId}:deactivate")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> DeactivateOperator(string operatorId)
        {
            if (!ObjectId.TryParse(operatorId, out var oid)) return BadRequest();
            var u = await _users.FindByIdAsync(oid); if (u == null) return NotFound();
            u.Status = "deactivated_by_admin"; u.UpdatedAt = DateTime.UtcNow;
            await _users.ReplaceAsync(u);
            return Ok();
        }

        [HttpPost("users/operators/{operatorId}:reactivate")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> ReactivateOperator(string operatorId)
        {
            if (!ObjectId.TryParse(operatorId, out var oid)) return BadRequest();
            var u = await _users.FindByIdAsync(oid); if (u == null) return NotFound();
            u.Status = "active"; u.UpdatedAt = DateTime.UtcNow;
            await _users.ReplaceAsync(u);
            return Ok();
        }

        [HttpGet("users/operators")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> GetActiveOperators()
        {
            var list = await _users.GetActiveOperatorsAsync();
            return Ok(list.Select(u => new { _id = u.Id.ToString(), u.Nic, u.Name, u.Email, u.Phone, u.Status }));
        }

        [HttpGet("users/operators/all")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> GetAllOperators()
        {
            var list = await _users.GetAllOperatorsAsync();
            return Ok(list.Select(u => new { _id = u.Id.ToString(), u.Nic, u.Name, u.Email, u.Phone, u.Status }));
        }

        [HttpGet("users/owners")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> Owners([FromQuery] string? nic)
        {
            var list = await _users.OwnersByNic(nic).ToListAsync();
            return Ok(list.Select(u => new { id = u.Id.ToString(), u.Nic, u.Name, u.Email, u.Phone, u.Status }));
        }

        [HttpPost("users/owners")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> CreateOwner([FromBody] CreateOwnerDto dto)
        {
            var password = string.IsNullOrEmpty(dto.Password) ? "owner123" : dto.Password;
            var u = new User
            {
                Id = ObjectId.GenerateNewId(),
                Role = Roles.EvOwner,
                Nic = dto.Nic,
                Name = dto.Name,
                Phone = dto.Phone,
                Email = dto.Email,
                PasswordHash = Services.AuthService.HashPassword(password)
            };
            await _users.CreateAsync(u);
            return Ok(new { _id = u.Id.ToString() });
        }

        [HttpPatch("users/owners/{ownerId}")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> PatchOwner(string ownerId, [FromBody] PatchOwnerDto dto)
        {
            if (!ObjectId.TryParse(ownerId, out var oid)) return BadRequest();
            var u = await _users.FindByIdAsync(oid); if (u == null) return NotFound();
            if (!string.IsNullOrEmpty(dto.Name)) u.Name = dto.Name;
            if (!string.IsNullOrEmpty(dto.Phone)) u.Phone = dto.Phone;
            if (!string.IsNullOrEmpty(dto.Email)) u.Email = dto.Email;
            u.UpdatedAt = DateTime.UtcNow;
            await _users.ReplaceAsync(u);
            return Ok(new { _id = u.Id.ToString(), u.Name, u.Phone, u.Email, u.Status });
        }

        [HttpPost("users/owners/{ownerId}:deactivate")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> DeactivateOwner(string ownerId)
        {
            if (!ObjectId.TryParse(ownerId, out var oid)) return BadRequest();
            var u = await _users.FindByIdAsync(oid); if (u == null) return NotFound();
            u.Status = "deactivated_by_admin"; u.UpdatedAt = DateTime.UtcNow;
            await _users.ReplaceAsync(u);
            return Ok();
        }

        [HttpPost("users/owners/{ownerId}:reactivate")]
        [Authorize(Policy = Policies.BackofficeOnly)]
        public async Task<IActionResult> ReactivateOwner(string ownerId)
        {
            if (!ObjectId.TryParse(ownerId, out var oid)) return BadRequest();
            var u = await _users.FindByIdAsync(oid); if (u == null) return NotFound();
            u.Status = "active"; u.UpdatedAt = DateTime.UtcNow;
            await _users.ReplaceAsync(u);
            return Ok();
        }

        [HttpGet("me")]
        [Authorize] // Allow both OwnerOnly and OperatorOnly
        public async Task<IActionResult> MeGet()
        {
            var sub = User.FindFirst("sub")?.Value
                   ?? User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(sub)) return Unauthorized(new { message = "Invalid token: no sub claim" });

            if (!ObjectId.TryParse(sub, out var oid)) return BadRequest(new { message = "Invalid user id in token" });

            var u = await _users.FindByIdAsync(oid);
            if (u == null) return NotFound(new { message = "User not found" });

            // Return user details for both EV Owners and Station Operators
            return Ok(new { 
                _id = u.Id.ToString(), 
                u.Name, 
                u.Phone, 
                u.Email,
                u.Nic,
                u.Status,
                u.Role,
                createdAt = u.CreatedAt,
                updatedAt = u.UpdatedAt
            });
        }

        [HttpPatch("me")]
        [Authorize] // Allow both EV owners and station operators
        public async Task<IActionResult> MePatch([FromBody] PatchOwnerDto dto) // Note: PatchOwnerDto is used for all user types
        {
            var sub = User.FindFirst("sub")?.Value
                   ?? User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(sub)) return Unauthorized(new { message = "Invalid token: no sub claim" });

            if (!ObjectId.TryParse(sub, out var oid)) return BadRequest(new { message = "Invalid user id in token" });

            var u = await _users.FindByIdAsync(oid);
            if (u == null) return NotFound(new { message = "User not found" });

            // Validate that at least one field is provided
            if (string.IsNullOrWhiteSpace(dto.Name) && string.IsNullOrWhiteSpace(dto.Phone) && 
                string.IsNullOrWhiteSpace(dto.Email) && string.IsNullOrWhiteSpace(dto.Nic))
            {
                return BadRequest(new { message = "At least one field (name, phone, email, or nic) must be provided for update" });
            }

            // Check if email is being updated and if it's already in use by another user
            if (!string.IsNullOrWhiteSpace(dto.Email) && dto.Email.Trim().ToLowerInvariant() != u.Email?.ToLowerInvariant())
            {
                var existingUser = await _users.FindByEmailAsync(dto.Email.Trim().ToLowerInvariant());
                if (existingUser != null && existingUser.Id != u.Id)
                {
                    return Conflict(new { message = "Email address is already in use by another user" });
                }
            }

            // Check if NIC is being updated and if it's already in use by another user
            if (!string.IsNullOrWhiteSpace(dto.Nic) && dto.Nic.Trim() != u.Nic)
            {
                var existingUser = await _users.FindByNicAsync(dto.Nic.Trim());
                if (existingUser != null && existingUser.Id != u.Id)
                {
                    return Conflict(new { message = "NIC is already in use by another user" });
                }
            }

            // Update fields only if they are provided and not empty
            if (!string.IsNullOrWhiteSpace(dto.Name)) u.Name = dto.Name.Trim();
            if (!string.IsNullOrWhiteSpace(dto.Phone)) u.Phone = dto.Phone.Trim();
            if (!string.IsNullOrWhiteSpace(dto.Email)) u.Email = dto.Email.Trim().ToLowerInvariant();
            if (!string.IsNullOrWhiteSpace(dto.Nic)) u.Nic = dto.Nic.Trim();
            u.UpdatedAt = DateTime.UtcNow;

            try
            {
                await _users.ReplaceAsync(u);
                return Ok(new { 
                    _id = u.Id.ToString(), 
                    u.Name, 
                    u.Phone, 
                    u.Email,
                    u.Nic,
                    u.Status,
                    u.Role,
                    updatedAt = u.UpdatedAt
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to update profile", error = ex.Message });
            }
        }



        [HttpPost("me:deactivate")]
        [Authorize] // Allow both EV owners and station operators
        public async Task<IActionResult> MeDeactivate()
        {
            var sub = User.FindFirst("sub")?.Value
                   ?? User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(sub)) return Unauthorized(new { message = "Invalid token: no sub claim" });
            var oid = ObjectId.Parse(sub);
            var u = await _users.FindByIdAsync(oid); if (u == null) return NotFound();
            u.Status = "deactivated_by_user"; u.UpdatedAt = DateTime.UtcNow;
            await _users.ReplaceAsync(u);
            return Ok();
        }
    }
}