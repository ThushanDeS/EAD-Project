namespace pulsecharge.Dtos
{
    public record CreateBookingDto(string StationId, string Date, DateTime StartTime, int SlotNumber, DateTime? EndTime = null);
    public record UpdateBookingDto(DateTime? StartTime, int? SlotNumber, DateTime? EndTime = null);
    public record OperatorFinalizeDto(double EnergyKWh, string? Notes);
    public record OperatorScanDto(string Qr);
    
    // DTOs for QR code responses
    public record BookingQrResponseDto(
        string BookingId,
        string QrCode,
        string? QrImageBase64,
        string? QrImageContentType
    );
    
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
    public record BackofficeCreateBookingDto(string OwnerId, string StationId, DateTime StartTime, DateTime EndTime, int SlotNumber);

}
