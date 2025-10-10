
namespace pulsecharge.Dtos
{
    public record LoginStaffDto(string? Email, string? Nic, string Password);
    public record RegisterOwnerDto(
        string Nic,
        string Name,
        string? Phone,
        string Password,
        string? Email
    );
}
