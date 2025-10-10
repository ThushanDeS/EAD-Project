/*
* File: User.cs
* Description: Defines the User model for the PulseCharge system, representing
*              both station operators and EV owners with account and status details.
*/

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace pulsecharge.Models
{
    public class User
    {
        [BsonId]
        public ObjectId Id { get; set; }  // Unique identifier for the user document

        public string Role { get; set; } = default!;  // Role of the user (e.g., Operator, Owner)
        public string? Email { get; set; }  // User’s email address (optional)
        public string? Nic { get; set; }  // National Identity Card number (optional)
        public string PasswordHash { get; set; } = default!;  // Hashed password for authentication
        public string Name { get; set; } = default!;  // Full name of the user
        public string? Phone { get; set; }  // Contact phone number (optional)
        public string Status { get; set; } = "active";  // Account status (e.g., active, deactivated)
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;  // Account creation timestamp
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;  // Last update timestamp
    }
}
