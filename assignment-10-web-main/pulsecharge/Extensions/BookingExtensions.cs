using pulsecharge.Models;
using pulsecharge.Dtos;

namespace pulsecharge.Extensions
{
    public static class BookingExtensions
    {
        public static BookingResponseDto ToResponseDto(this Booking booking)
        {
            return new BookingResponseDto(
                booking.Id.ToString(),
                booking.StationId.ToString(),
                booking.SlotNumber,
                booking.OwnerId.ToString(),
                booking.StartTime,
                booking.EndTime,
                booking.Status,
                booking.QrCode,
                booking.QrImageBase64,
                booking.QrImageContentType,
                booking.CreatedBy,
                booking.CreatedAt,
                booking.UpdatedAt,
                booking.FinalizedBy?.ToString(),
                booking.FinalizedAt,
                booking.EnergyKWh,
                booking.Notes
            );
        }

        public static BookingQrResponseDto ToQrResponseDto(this Booking booking)
        {
            return new BookingQrResponseDto(
                booking.Id.ToString(),
                booking.QrCode ?? "",
                booking.QrImageBase64,
                booking.QrImageContentType
            );
        }
    }
}