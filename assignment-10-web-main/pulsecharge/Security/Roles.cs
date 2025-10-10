/*
* File: Roles.cs
* Description: Defines user roles and authorization policies used across the system.
*              Provides constants for consistent role and policy references.
*/

namespace pulsecharge.Security
{
    // Contains constant role names assigned to users
    public static class Roles
    {
        public const string Backoffice = "Backoffice";           // Administrative users
        public const string StationOperator = "StationOperator"; // Charging station managers
        public const string EvOwner = "EvOwner";                 // Electric vehicle owners
    }

    // Defines authorization policy names mapped to roles
    public static class Policies
    {
        public const string BackofficeOnly = "BackofficeOnly"; // Restricts access to backoffice users
        public const string OperatorOnly = "OperatorOnly";     // Restricts access to station operators
        public const string OwnerOnly = "OwnerOnly";           // Restricts access to EV owners
    }
}
