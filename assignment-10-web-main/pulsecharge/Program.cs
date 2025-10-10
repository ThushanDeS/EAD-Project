/*
* File: Program.cs
* Description: Main entry point for the PulseCharge API application. Configures dependency injection,
*              authentication, authorization, Swagger, CORS, and middleware pipeline.
*/

using System.IdentityModel.Tokens.Jwt;
using pulsecharge.Mongo;
using pulsecharge.Security;
using pulsecharge.Services;
using pulsecharge.Repositories;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using System.Text;

JwtSecurityTokenHandler.DefaultInboundClaimTypeMap.Clear();

var builder = WebApplication.CreateBuilder(args);

// Retrieve JWT configuration values from appsettings/environment.
// Jwt:Key is required; other values have sane defaults.
var jwtKey = builder.Configuration["Jwt:Key"] ?? throw new InvalidOperationException("Jwt:Key missing");
var jwtIssuer = builder.Configuration["Jwt:Issuer"] ?? "Evcs";
var jwtAudience = builder.Configuration["Jwt:Audience"] ?? "EvcsClients";

// Register MongoDB context and indexing as singletons; indexing will ensure required indexes exist at startup.
builder.Services.AddSingleton<MongoContext>();
builder.Services.AddSingleton<Indexing>();

// Register repositories and services with scoped lifetimes (per-request).
builder.Services.AddScoped<UserRepository>();
builder.Services.AddScoped<StationRepository>();
builder.Services.AddScoped<BookingRepository>();
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<BookingService>();
builder.Services.AddScoped<AvailabilityService>();
builder.Services.AddScoped<QrCodeService>();

// Configure JWT authentication. Tokens are validated for issuer/audience/lifetime and the signing key.
builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer = jwtIssuer,
            ValidAudience = jwtAudience,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey)),
            // Map role and name claim types for easier access via User.FindFirst
            RoleClaimType = System.Security.Claims.ClaimTypes.Role,
            NameClaimType = System.Security.Claims.ClaimTypes.NameIdentifier
        };

        // Helpful debug hook: prints claims after successful validation when running locally.
        options.Events = new JwtBearerEvents
        {
            OnTokenValidated = context =>
            {
                Console.WriteLine("JWT Claims after validation:");
                foreach (var claim in context.Principal!.Claims)
                {
                    Console.WriteLine($"  {claim.Type}: {claim.Value}");
                }
                return Task.CompletedTask;
            }
        };
    });

// Configure authorization policies used across controllers.
builder.Services.AddAuthorization(options =>
{
    // Policies are named and map to roles defined in Security/Roles.cs
    options.AddPolicy(Policies.BackofficeOnly, p => p.RequireRole(Roles.Backoffice));
    options.AddPolicy(Policies.OperatorOnly, p => p.RequireRole(Roles.StationOperator));
    options.AddPolicy(Policies.OwnerOnly, p => p.RequireRole(Roles.EvOwner));
});

// Swagger generation with Bearer auth definition so developers can try endpoints with JWTs.
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo { Title = "EVCS API", Version = "v1" });
    c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        In = ParameterLocation.Header,
        Description = "JWT Authorization header using the Bearer scheme.",
        Name = "Authorization",
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT"
    });
    c.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference { Type = ReferenceType.SecurityScheme, Id = "Bearer" }
            },
            Array.Empty<string>()
        }
    });
});

// Add controller support
builder.Services.AddControllers();

// Add a permissive CORS policy used by the frontend and mobile clients during development.
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll",
        policy =>
        {
            policy.AllowAnyOrigin()
                  .AllowAnyMethod()
                  .AllowAnyHeader();
        });
});

var app = builder.Build();

// Ensure MongoDB indexes are created before serving traffic.
app.Services.GetRequiredService<Indexing>().EnsureAll();

// In development enable Swagger UI at the app root for quick exploration.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "EVCS API v1");
        c.RoutePrefix = string.Empty;
    });
}

// Enable CORS, Authentication, and Authorization middlewares in the correct order.
app.UseCors("AllowAll");

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

// simple health endpoint for load balancers/monitoring
app.MapGet("/api/v1/health", () => Results.Ok(new { status = "ok", time = DateTime.UtcNow }));

app.Run();
