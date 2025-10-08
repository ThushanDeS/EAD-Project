
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace pulsecharge.Security
{
    public class JwtService
    {
        private readonly IConfiguration _configuration;
        public JwtService (IConfiguration configuration)
        {
            _configuration = configuration;
        }

        public string CreateToken(string userId, string role, string? ownerNic = null, IEnumerable<string>? stationIds  = null)
        {
            var issuer = _configuration["Jwt:Issuer"] ?? "PulseChargeIssuer";
            var audience = _configuration["Jwt:Audience"] ?? "PulseChargeAudience";
            var key = _configuration["Jwt:Key"] ?? throw new InvalidOperationException("Missing JWT key configuration");

            var claims = new List<Claim>
            {
                new(JwtRegisteredClaimNames.Sub, userId),
                new("role", role)
            };
            if (!string.IsNullOrEmpty(ownerNic))
                claims.Add(new("ownerNic", ownerNic));
            if (stationIds  != null)
                foreach (var sid in stationIds )
                    claims.Add(new("operatorStationId", sid));

            var creds = new SigningCredentials(new SymmetricSecurityKey(Encoding.UTF8.GetBytes(key)), SecurityAlgorithms.HmacSha256);
            var token = new JwtSecurityToken(issuer: issuer, audience: audience, claims: claims, expires: DateTime.UtcNow.AddDays(7), signingCredentials: creds);
            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }
}
