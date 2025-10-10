/*
* File: AuthService.cs
* Description: Provides authentication and registration services for users.
*              Handles login, password hashing, and JWT token generation.
*/

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
        private readonly UserRepository _users; // Repository for user data access
        private readonly JwtService _jwt;       // Service for generating JWT tokens

        // Constructor initializes dependencies for user management and token generation
        public AuthService(UserRepository users, IConfiguration config)
        {
            _users = users;
            _jwt = new JwtService(config);
        }

        // Generates a SHA256 hash for the provided string (used for password hashing)
        private static string Hash(string s)
        {
            using var sha = SHA256.Create();
            return Convert.ToHexString(sha.ComputeHash(Encoding.UTF8.GetBytes(s)));
        }

        // Authenticates a user using email or NIC and returns a JWT token if valid
        public async Task<(string token, User user)?> LoginAsync(string? email, string? nic, string password)
        {
            User? user = null;

            // Attempt to find user by email or NIC
            if (!string.IsNullOrEmpty(email))
                user = await _users.FindByEmailAsync(email);
            else if (!string.IsNullOrEmpty(nic))
                user = await _users.FindByNicAsync(nic);

            // Validate user credentials
            if (user == null) return null;
            if (user.PasswordHash != Hash(password)) return null;
            if (user.Role == Roles.EvOwner && user.Status != "active") return null;

            // Generate JWT token upon successful login
            var token = _jwt.CreateToken(user.Id.ToString(), user.Role, user.Nic, null);
            return (token, user);
        }

        // Registers a new EV owner and issues a JWT token upon success
        public async Task<(string token, User user)> RegisterOwnerAsync(string nic, string name, string? phone, string password, string? email)
        {
            // Ensure NIC is unique before creating a new owner
            var exists = await _users.FindByNicAsync(nic);
            if (exists != null) throw new InvalidOperationException("NIC already exists");

            // Create new user record
            var u = new User
            {
                Id = ObjectId.GenerateNewId(),
                Role = Roles.EvOwner,
                Nic = nic,
                Name = name,
                Phone = phone,
                PasswordHash = Hash(password),
                Email = email
            };

            await _users.CreateAsync(u);

            // Generate JWT token for the newly registered owner
            var token = _jwt.CreateToken(u.Id.ToString(), u.Role, u.Nic, null);
            return (token, u);
        }

        // Public utility method to hash passwords externally if needed
        public static string HashPassword(string password) => Hash(password);
    }
}
