using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace pulsecharge.Models
{
    /// <summary>
    /// Represents a single booking/reservation for an EV charging station.
    /// This model is persisted to the MongoDB "bookings" collection.
    /// </summary>
    [BsonIgnoreExtraElements]
    public class Booking
    {
        /// <summary>
        /// Primary key (MongoDB ObjectId).
        /// </summary>
        [BsonId] public ObjectId Id { get; set; }

        /// <summary>
        /// The station this booking belongs to.
        /// Stored as an ObjectId referencing the station document.
        /// </summary>
        [BsonElement("StationId")] public ObjectId StationId { get; set; }

        /// <summary>
        /// The numeric slot within the station that is reserved.
        /// </summary>
        [BsonElement("SlotNumber")] public int SlotNumber { get; set; }

        /// <summary>
        /// The owner (user) who created the booking.
        /// Stored as an ObjectId referencing the users collection.
        /// </summary>
        [BsonElement("OwnerId")] public ObjectId OwnerId { get; set; }

        /// <summary>
        /// Booking start time (UTC). The service aligns bookings to the top of the hour.
        /// </summary>
        [BsonElement("StartTime")] public DateTime StartTime { get; set; }

        /// <summary>
        /// Booking end time (UTC).
        /// </summary>
        [BsonElement("EndTime")] public DateTime EndTime { get; set; }

        /// <summary>
        /// Current status of the booking. Typical values: "pending", "approved", "cancelled", "rejected".
        /// Defaults to "pending" when created by an EV owner; backoffice-created bookings may be pre-approved.
        /// </summary>
        [BsonElement("Status")] public string Status { get; set; } = "pending";

        /// <summary>
        /// The encoded payload used for QR-based verification. Not all endpoints return this to callers.
        /// </summary>
        [BsonElement("QrCode")] public string? QrCode { get; set; }

        /// <summary>
        /// Base64-encoded image data (e.g. PNG) representing the QR code.
        /// Useful for returning as an inline image in APIs.
        /// </summary>
        [BsonElement("QrImageBase64")] public string? QrImageBase64 { get; set; }

        /// <summary>
        /// Content type for the QR image (default: image/png).
        /// </summary>
        [BsonElement("QrImageContentType")] public string? QrImageContentType { get; set; } = "image/png";

        /// <summary>
        /// Who created the booking. Example values: "EvOwner", "Backoffice".
        /// Used to determine initial approval behavior.
        /// </summary>
        [BsonElement("CreatedBy")] public string CreatedBy { get; set; } = "EvOwner";

        /// <summary>
        /// Timestamp for when the booking was created (UTC).
        /// </summary>
        [BsonElement("CreatedAt")] public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        /// <summary>
        /// Timestamp for the last update to the booking (UTC).
        /// </summary>
        [BsonElement("UpdatedAt")] public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        /// <summary>
        /// If the booking is finalized (e.g. completed), this stores the operator's ObjectId who finalized it.
        /// </summary>
        [BsonElement("FinalizedBy")] public ObjectId? FinalizedBy { get; set; }

        /// <summary>
        /// When the booking was finalized (UTC).
        /// </summary>
        [BsonElement("FinalizedAt")] public DateTime? FinalizedAt { get; set; }

        /// <summary>
        /// Energy consumed during the session in kWh. Optional and may be populated after completion.
        /// </summary>
        [BsonElement("EnergyKWh")] public double? EnergyKWh { get; set; }

        /// <summary>
        /// Optional notes attached to the booking.
        /// </summary>
        [BsonElement("Notes")] public string? Notes { get; set; }
    }
}
