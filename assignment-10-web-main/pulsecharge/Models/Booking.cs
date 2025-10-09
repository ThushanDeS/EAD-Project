using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace pulsecharge.Models
{
    [BsonIgnoreExtraElements]
    public class Booking
    {
        [BsonId] public ObjectId Id { get; set; }
        [BsonElement("StationId")] public ObjectId StationId { get; set; }
        [BsonElement("SlotNumber")] public int SlotNumber { get; set; }
        [BsonElement("OwnerId")] public ObjectId OwnerId { get; set; }
        [BsonElement("StartTime")] public DateTime StartTime { get; set; }
        [BsonElement("EndTime")] public DateTime EndTime { get; set; }
        [BsonElement("Status")] public string Status { get; set; } = "pending";
        [BsonElement("QrCode")] public string? QrCode { get; set; }
        [BsonElement("QrImageBase64")] public string? QrImageBase64 { get; set; }
        [BsonElement("QrImageContentType")] public string? QrImageContentType { get; set; } = "image/png";
        [BsonElement("CreatedBy")] public string CreatedBy { get; set; } = "EvOwner";
        [BsonElement("CreatedAt")] public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        [BsonElement("UpdatedAt")] public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
        [BsonElement("FinalizedBy")] public ObjectId? FinalizedBy { get; set; }
        [BsonElement("FinalizedAt")] public DateTime? FinalizedAt { get; set; }
        [BsonElement("EnergyKWh")] public double? EnergyKWh { get; set; }
        [BsonElement("Notes")] public string? Notes { get; set; }
    }
}
