
using pulsecharge.Dtos;
using pulsecharge.Services;
using Microsoft.AspNetCore.Mvc;

namespace pulsecharge.Controllers
{
    [ApiController]
    [Route("api/v1/auth")]
    public class AuthController : ControllerBase
    {
        private readonly AuthService _authService;
        public AuthController(AuthService auth){ _authService = auth; }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginStaffDto dto)
        {
            var result  = await _authService.LoginAsync(dto.Email, dto.Nic, dto.Password);
            if (result == null)
            {
                return Unauthorized(new
                {
                    code = "E_AUTH",
                    message = "Invalid credentials or inactive account"
                });
            }

            var (jwtToken, account) = result.Value;
            return Ok(new
            {
                token = jwtToken,
                user = new
                {
                    _id = account.Id.ToString(),
                    role = account.Role,
                    name = account.Name
                }
            });
        }

        [HttpPost("register/owner")]
        public async Task<IActionResult> RegisterOwner([FromBody] RegisterOwnerDto dto)
        {
            try
            {
                var (jwtToken, account) = await _authService.RegisterOwnerAsync(dto.Nic, dto.Name, dto.Phone, dto.Password, dto.Email);
                return Ok(new
                {
                    token = jwtToken,
                    user = new
                    {
                        _id = account.Id.ToString(),
                        role = account.Role,
                        name = account.Name
                    }
                });
            }
            catch (Exception ex)
            {
                return Conflict(new { code="E_DUP_NIC", message=ex.Message });
            }
        }
    }
}
