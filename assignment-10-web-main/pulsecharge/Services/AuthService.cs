
using pulsecharge.Models;
using pulsecharge.Repositories;
using pulsecharge.Security;
using MongoDB.Bson;
using System.Security.Cryptography;
using System.Text;

namespace pulsecharge.Services
{
    public class AuthService
    {
        private readonly UserRepository _users;
        private readonly JwtService _jwt;
        public AuthService(UserRepository users, IConfiguration config)
        {
            _users = users;
            _jwt = new JwtService(config);
        }

        private static string Hash(string s)
        {
            using var sha = SHA256.Create();
            return Convert.ToHexString(sha.ComputeHash(Encoding.UTF8.GetBytes(s)));
        }

        public async Task<(string token, User user)?> LoginAsync(string? email, string? nic, string password)
        {
            User? user = null;
            if (!string.IsNullOrEmpty(email)) user = await _users.FindByEmailAsync(email);
            else if (!string.IsNullOrEmpty(nic)) user = await _users.FindByNicAsync(nic);

            if (user == null) return null;
            if (user.PasswordHash != Hash(password)) return null;
            if (user.Role == Roles.EvOwner && user.Status != "active") return null;

            var token = _jwt.CreateToken(user.Id.ToString(), user.Role, user.Nic, null);
            return (token, user);
        }

        public async Task<(string token, User user)> RegisterOwnerAsync(string nic, string name, string? phone, string password, string? email)
        {
            var exists = await _users.FindByNicAsync(nic);
            if (exists != null) throw new InvalidOperationException("NIC already exists");
            var u = new User { Id = ObjectId.GenerateNewId(), Role = Roles.EvOwner, Nic = nic, Name = name, Phone = phone, PasswordHash = Hash(password), Email = email };
            await _users.CreateAsync(u);
            var token = _jwt.CreateToken(u.Id.ToString(), u.Role, u.Nic, null);
            return (token, u);
        }

        public static string HashPassword(string password) => Hash(password);
    }
}
