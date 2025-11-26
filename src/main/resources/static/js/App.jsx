import React, { useState, useEffect } from "react";
import ReactDOM from "react-dom/client";
import { Sun, Moon } from "lucide-react";

const { API_BASE_URL } = window.AppConfig;
// Destructure components from window objects where they are attached
const { LoginForm, SignupForm, VerificationHandler, TotpLogin } = window.Auth;
const Dashboard = window.Dashboard; // Ensure Dashboard is picked up

// Helper to safely decode JWT without external libraries
const parseJwt = (token) => {
	try {
		const base64Url = token.split(".")[1];
		const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
		const jsonPayload = decodeURIComponent(
			window
				.atob(base64)
				.split("")
				.map(function (c) {
					return "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2);
				})
				.join(""),
		);
		return JSON.parse(jsonPayload);
	} catch (e) {
		return null;
	}
};

const App = () => {
	// Views: 'login' | 'signup' | 'verifying' | 'totp-login' | 'dashboard'
	const [view, setView] = useState("login");
	const [isDarkMode, setIsDarkMode] = useState(false);
	const [currentUser, setCurrentUser] = useState(null);
	const [token, setToken] = useState(localStorage.getItem("authToken"));
	const [verificationToken, setVerificationToken] = useState(null);
	const [pendingTotp, setPendingTotp] = useState(null);

	useEffect(() => {
		// 1. Check for Dark Mode preference
		if (window.matchMedia("(prefers-color-scheme: dark)").matches) {
			setIsDarkMode(true);
		}

		// 2. Check for Verification Token in URL
		const params = new URLSearchParams(window.location.search);
		const urlToken = params.get("token");

		if (urlToken) {
			setVerificationToken(urlToken);
			setView("verifying");
			window.history.replaceState({}, document.title, window.location.pathname);
		} else if (token) {
			// 3. Restore Session from Token
			const decoded = parseJwt(token);

			if (decoded && decoded.exp * 1000 > Date.now()) {
				// Token is valid and not expired
				// The 'sub' field in JWT standard usually holds the username/subject
				const username = decoded.sub;
				setCurrentUser({ username });
				setView("dashboard");
			} else {
				// Token is invalid or expired
				console.warn("Session expired or invalid token.");
				localStorage.removeItem("authToken");
				setToken(null);
				setView("login");
			}
		}
	}, []); // Run only once on mount

	// Effect to sync token state with localStorage
	useEffect(() => {
		if (token) {
			localStorage.setItem("authToken", token);
		} else {
			localStorage.removeItem("authToken");
		}
	}, [token]);

	const handleLoginSuccess = (username, authToken) => {
		setCurrentUser({ username });
		setToken(authToken);
		setView("dashboard");
	};

	const handleTotpRequired = (userId, authToken, username) => {
		setPendingTotp({ userId, token: authToken, username });
		setView("totp-login");
	};

	const handleTotpVerified = (username, authToken) => {
		setCurrentUser({ username });
		setToken(authToken);
		setPendingTotp(null);
		setView("dashboard");
	};

	const handleLogout = async () => {
		try {
			if (token) {
				await fetch(`${API_BASE_URL}/user-panel/logout`, {
					method: "GET",
					headers: { Authorization: `Bearer ${token}` },
				});
			}
		} catch (e) {
			console.error("Logout failed", e);
		}
		setToken(null);
		setCurrentUser(null);
		setView("login");
	};

	const toggleTheme = () => setIsDarkMode(!isDarkMode);

	// Fallback if Dashboard component didn't load for some reason
	if (view === "dashboard" && !Dashboard) {
		return (
			<div className="p-10 text-center">
				Loading Dashboard Component... (Please refresh if stuck)
			</div>
		);
	}

	return (
		<div className={isDarkMode ? "dark" : ""}>
			<div
				className={`min-h-screen transition-colors duration-300 ${
					view === "dashboard"
						? "bg-gray-50 dark:bg-gray-900"
						: "bg-gradient-to-br from-indigo-50 via-white to-purple-50 dark:from-gray-900 dark:via-gray-900 dark:to-slate-900 flex items-center justify-center p-4 md:p-6"
				}`}
			>
				{view === "dashboard" ? (
					<Dashboard
						user={currentUser || { username: "User" }}
						token={token}
						onLogout={handleLogout}
						isDarkMode={isDarkMode}
						toggleTheme={toggleTheme}
					/>
				) : (
					<div className="w-full max-w-md bg-white dark:bg-gray-800 rounded-2xl shadow-xl dark:shadow-2xl dark:shadow-black/50 overflow-hidden transition-colors duration-300 border border-transparent dark:border-gray-700 animate-fade-in">
						<div className="p-6 md:p-8">
							{/* Floating toggle only for non-dashboard screens */}
							<button
								type="button"
								onClick={toggleTheme}
								className="absolute top-4 right-4 p-2 rounded-full text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
							>
								{isDarkMode ? <Sun size={20} /> : <Moon size={20} />}
							</button>

							{view === "login" && (
								<LoginForm
									onSwitchMode={() => setView("signup")}
									onLoginSuccess={handleLoginSuccess}
									onTotpRequired={handleTotpRequired}
								/>
							)}
							{view === "totp-login" && pendingTotp && (
								<TotpLogin
									userId={pendingTotp.userId}
									token={pendingTotp.token}
									username={pendingTotp.username}
									onVerified={handleTotpVerified}
									onCancel={() => setView("login")}
								/>
							)}
							{view === "signup" && (
								<SignupForm onSwitchMode={() => setView("login")} />
							)}
							{view === "verifying" && (
								<VerificationHandler
									token={verificationToken}
									onVerificationComplete={() => setView("login")}
								/>
							)}
						</div>
					</div>
				)}
			</div>
		</div>
	);
};

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(<App />);
