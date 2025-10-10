/*
* File: JwtService.cs
* Description: Handles creation of JSON Web Tokens (JWT) for user authentication.
*              Reads configuration values, adds claims, and signs tokens securely.
*/

using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace pulsecharge.Security
{
    public class JwtService
    {
        private readonly IConfiguration _configuration;

        // Injects configuration to access JWT settings (Issuer, Audience, Key)
        public JwtService(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        // Generates a JWT token with user and role claims
        public string CreateToken(string userId, string role, string? ownerNic = null, IEnumerable<string>? stationIds = null)
        {
            // Load JWT configuration values
            var issuer = _configuration["Jwt:Issuer"] ?? "PulseChargeIssuer";
            var audience = _configuration["Jwt:Audience"] ?? "PulseChargeAudience";
            var key = _configuration["Jwt:Key"] ?? throw new InvalidOperationException("Missing JWT key configuration");

            // Define token claims
            var claims = new List<Claim>
            {
                new(JwtRegisteredClaimNames.Sub, userId), // Subject (User ID)
                new("role", role) // User role
            };

            // Optional claim for EV owner NIC
            if (!string.IsNullOrEmpty(ownerNic))
                claims.Add(new("ownerNic", ownerNic));

            // Optional claims for operator station IDs
            if (stationIds != null)
                foreach (var sid in stationIds)
                    claims.Add(new("operatorStationId", sid));

            // Create signing credentials using secret key
            var creds = new SigningCredentials(
                new SymmetricSecurityKey(Encoding.UTF8.GetBytes(key)),
                SecurityAlgorithms.HmacSha256
            );

            // Build the JWT token with claims, expiration, and signature
            var token = new JwtSecurityToken(
                issuer: issuer,
                audience: audience,
                claims: claims,
                expires: DateTime.UtcNow.AddDays(7),
                signingCredentials: creds
            );

            // Return serialized token string
            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }
}
