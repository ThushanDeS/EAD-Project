const Unauthorized = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full text-center">
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">403</h1>
          <h2 className="text-xl font-semibold text-gray-700 mb-2">Unauthorized Access</h2>
          <p className="text-gray-600">
            You don't have permission to access this page. Please contact your administrator if you believe this is an error.
          </p>
        </div>
        <a
          href="/dashboard"
          className="btn-primary inline-block"
        >
          Go to Dashboard
        </a>
      </div>
    </div>
  );
};

export default Unauthorized;