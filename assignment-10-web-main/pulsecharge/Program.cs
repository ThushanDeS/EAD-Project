using System.IdentityModel.Tokens.Jwt;
using pulsecharge.Mongo;
using pulsecharge.Security;
using pulsecharge.Services;
using pulsecharge.Repositories;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using System.Text;

// Prevent automatic claim type mapping so JWT claims appear as the token contains them
JwtSecurityTokenHandler.DefaultInboundClaimTypeMap.Clear();

var builder = WebApplication.CreateBuilder(args);

var jwtKey = builder.Configuration["Jwt:Key"] ?? throw new InvalidOperationException("Jwt:Key missing");
var jwtIssuer = builder.Configuration["Jwt:Issuer"] ?? "Evcs";
var jwtAudience = builder.Configuration["Jwt:Audience"] ?? "EvcsClients";

builder.Services.AddSingleton<MongoContext>();
builder.Services.AddSingleton<Indexing>();

builder.Services.AddScoped<UserRepository>();
builder.Services.AddScoped<StationRepository>();
builder.Services.AddScoped<BookingRepository>();
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<BookingService>();
builder.Services.AddScoped<AvailabilityService>();
builder.Services.AddScoped<QrCodeService>();

// Configure JWT authentication
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
            // Map the Role and NameIdentifier claims to common types used by ASP.NET
            RoleClaimType = System.Security.Claims.ClaimTypes.Role,
            NameClaimType = System.Security.Claims.ClaimTypes.NameIdentifier
        };

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

builder.Services.AddAuthorization(options =>
{
    options.AddPolicy(Policies.BackofficeOnly, p => p.RequireRole(Roles.Backoffice));
    options.AddPolicy(Policies.OperatorOnly, p => p.RequireRole(Roles.StationOperator));
    options.AddPolicy(Policies.OwnerOnly, p => p.RequireRole(Roles.EvOwner));
});

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

// Add CORS policy used by frontend during development (AllowAll). Consider tightening for production.
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

app.Services.GetRequiredService<Indexing>().EnsureAll();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "EVCS API v1");
        c.RoutePrefix = string.Empty;
    });
}

// Enable CORS, Authentication, and Authorization middlewares
app.UseCors("AllowAll");

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

// simple health endpoint
app.MapGet("/api/v1/health", () => Results.Ok(new { status = "ok", time = DateTime.UtcNow }));

app.Run();
