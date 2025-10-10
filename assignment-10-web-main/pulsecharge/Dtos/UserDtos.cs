namespace pulsecharge.Dtos
{
    public record CreateOperatorDto(string Email, string Name, string? Phone, string Password, string Nic);
    public record UpdateOperatorDto(string Email, string Name, string? Phone, string Nic);
    public record CreateOwnerDto(string Nic, string Name, string? Phone, string? Password = null, string? Email = null);
    public record PatchOwnerDto(string? Name, string? Phone, string? Email, string? Nic);
    
    // Auth DTOs
    public record LoginStaffDto(string Email, string Nic, string Password);
    public record RegisterOwnerDto(string Nic, string Name, string Phone, string Password, string Email);
}