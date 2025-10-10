import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { authAPI } from "../api/auth";
import loginBg from '../assets/login-bg.png';

const Login = () => {
  const [credentials, setCredentials] = useState({ EmailOrNic: "", Password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const { EmailOrNic, Password } = credentials;
      const isEmail = EmailOrNic.includes('@');

      const payload = {
        Email: isEmail ? EmailOrNic : null,
        Nic: !isEmail ? EmailOrNic : null,
        Password,
      };

      const response = await authAPI.login(payload);
      const { token, user } = response;

      if (!token || !user) {
        throw new Error("Invalid login response from server");
      }

      login(token, user);

      const userRole = user?.role || '';
      if (userRole.toLowerCase() === 'stationoperator') {
        navigate('/operator-dashboard', { replace: true });
      } else {
        navigate('/dashboard', { replace: true });
      }

    } catch (error) {
      setError(
        error.response?.data?.message ||
        error.message ||
        "Login failed. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setCredentials((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  return (
    <div className="flex h-screen bg-white">
      <div className="flex items-center justify-center flex-1 px-4 py-12 sm:px-6 lg:px-20 xl:px-24">
        <div className="w-full max-w-md mx-auto lg:mx-0">
          <div className="text-center">
            <h2 className="mt-6 text-3xl font-extrabold text-gray-900">
              EV Charging Management
            </h2>
            <p className="mt-2 text-sm text-gray-600">
              Sign in to your account
            </p>
          </div>

          <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
            {error && (
              <div className="px-4 py-3 text-red-700 border border-red-300 rounded bg-red-50">
                {error}
              </div>
            )}

            <div className="space-y-4">
              <div>
                <label htmlFor="EmailOrNic" className="block text-sm font-medium text-gray-700">
                  Email or NIC
                </label>
                <input
                  id="EmailOrNic"
                  name="EmailOrNic"
                  type="text"
                  required
                  className="input-field"
                  placeholder="Enter your email or NIC"
                  value={credentials.EmailOrNic}
                  onChange={handleChange}
                />
              </div>

              <div>
                <label htmlFor="Password" className="block text-sm font-medium text-gray-700">
                  Password
                </label>
                <input
                  id="Password"
                  name="Password"
                  type="password"
                  required
                  className="input-field"
                  placeholder="Enter your password"
                  value={credentials.Password}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div>
              <button
                type="submit"
                disabled={loading}
                className="w-full btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? "Signing in..." : "Sign in"}
              </button>
            </div>
          </form>
        </div>
      </div>
      <div className="hidden lg:flex lg:w-1/2">
        <img
          className="object-cover w-full h-full"
          src={loginBg}
          alt="EV charging station"
        />
      </div>
    </div>
  );
};

export default Login;