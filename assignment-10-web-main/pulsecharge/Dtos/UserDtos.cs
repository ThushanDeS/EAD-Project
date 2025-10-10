/*
* File: UserDtos.cs
* Description: Contains Data Transfer Objects (DTOs) for creating and updating
*              operator and EV owner accounts in the PulseCharge system.
*/

namespace pulsecharge.Dtos
{
    // DTO for creating a new station operator
    public record CreateOperatorDto(string Email, string Name, string? Phone, string Password, string Nic);

    // DTO for updating an existing station operator
    public record UpdateOperatorDto(string Email, string Name, string? Phone, string Nic);

    // DTO for creating a new EV owner (optional email and password)
    public record CreateOwnerDto(string Nic, string Name, string? Phone, string? Password = null, string? Email = null);

    // DTO for partially updating an EV owner's profile
    public record PatchOwnerDto(string? Name, string? Phone, string? Email, string? Nic);
}
