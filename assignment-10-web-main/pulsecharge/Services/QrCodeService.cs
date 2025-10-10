using QRCoder;
using System.Drawing;
using System.Drawing.Imaging;

namespace pulsecharge.Services
{
    public class QrCodeService
    {
        public (string qrText, string qrImageBase64) GenerateQrCode(string payload)
        {
            try
            {
                // Generate QR text (Base64 encoded payload)
                var qrText = Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes(payload));

                // Generate QR code image using the Base64 encoded text (same as what we store)
                var qrGenerator = new QRCodeGenerator();
                var qrCodeData = qrGenerator.CreateQrCode(qrText, QRCodeGenerator.ECCLevel.Q);
                var qrCode = new PngByteQRCode(qrCodeData);
                
                // Get QR code as PNG bytes
                var qrCodeBytes = qrCode.GetGraphic(20);
                var qrImageBase64 = Convert.ToBase64String(qrCodeBytes);

                return (qrText, qrImageBase64);
            }
            catch (Exception ex)
            {
                throw new Exception($"Failed to generate QR code: {ex.Message}", ex);
            }
        }

        public byte[] GetQrImageBytes(string base64Image)
        {
            try
            {
                return Convert.FromBase64String(base64Image);
            }
            catch (Exception ex)
            {
                throw new Exception($"Failed to convert base64 to bytes: {ex.Message}", ex);
            }
        }

        public bool ValidateQrCode(string qrText, string expectedPayload)
        {
            try
            {
                var decodedBytes = Convert.FromBase64String(qrText);
                var decodedPayload = System.Text.Encoding.UTF8.GetString(decodedBytes);
                return decodedPayload == expectedPayload;
            }
            catch
            {
                return false;
            }
        }

        public string DecodeQrCode(string base64QrText)
        {
            try
            {
                var decodedBytes = Convert.FromBase64String(base64QrText);
                return System.Text.Encoding.UTF8.GetString(decodedBytes);
            }
            catch (Exception ex)
            {
                throw new Exception($"Failed to decode QR code: {ex.Message}", ex);
            }
        }
    }
}