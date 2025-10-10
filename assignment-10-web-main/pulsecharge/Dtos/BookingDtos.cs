// BookingDtos.cs
// Lightweight data transfer objects used to communicate booking data across API boundaries.
// Keep these records simple and immutable so they can be serialized safely by ASP.NET Core.
namespace pulsecharge.Dtos
{
    // Sent by clients to request a new booking. StartTime and optional EndTime are expected in client-local
    // representation; controllers convert to UTC before handing to the service layer.
    public record CreateBookingDto(string StationId, string Date, DateTime StartTime, int SlotNumber, DateTime? EndTime = null);

    // Partial update payload for an existing booking. Null values indicate no change for that field.
    public record UpdateBookingDto(DateTime? StartTime, int? SlotNumber, DateTime? EndTime = null);

    // Sent by operator when finalizing a booking (charging complete)
    public record OperatorFinalizeDto(double EnergyKWh, string? Notes);

    // A simple DTO used when operators scan a QR code
    public record OperatorScanDto(string Qr);
    
    // DTOs for QR code responses returned by the API
    public record BookingQrResponseDto(
        string BookingId,
        string QrCode,
        string? QrImageBase64,
        string? QrImageContentType
    );
    
    // Full booking response dto used by GET endpoints; mirrors Booking model but exposes string IDs
    public record BookingResponseDto(
        string Id,
        string StationId,
        int SlotNumber,
        string OwnerId,
        DateTime StartTime,
        DateTime EndTime,
        string Status,
        string? QrCode,
        string? QrImageBase64,
        string? QrImageContentType,
        string CreatedBy,
        DateTime CreatedAt,
        DateTime UpdatedAt,
        string? FinalizedBy,
        DateTime? FinalizedAt,
        double? EnergyKWh,
        string? Notes
    );

    // Backoffice-only create DTO: backoffice can create bookings on behalf of any owner
    public record BackofficeCreateBookingDto(string OwnerId, string StationId, DateTime StartTime, DateTime EndTime, int SlotNumber);

}
