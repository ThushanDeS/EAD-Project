
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace pulsecharge.Models
{
    public class User
    {
        [BsonId]
        public ObjectId Id { get; set; }
        
        public string Role { get; set; } = default!;
        public string? Email { get; set; }
        public string? Nic { get; set; }
        public string PasswordHash { get; set; } = default!;
        public string Name { get; set; } = default!;
        public string? Phone { get; set; }
        public string Status { get; set; } = "active";
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    }
}
