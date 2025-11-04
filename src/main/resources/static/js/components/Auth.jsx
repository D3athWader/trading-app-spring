import React, { useState } from "react";
import {
	User,
	Lock,
	Mail,
	Globe,
	ArrowRight,
	CheckCircle2,
	RefreshCw,
	ShieldCheck,
} from "lucide-react";

const { API_BASE_URL } = window.AppConfig;
const { InputField, Button, Alert } = window.UI;

const LoginForm = ({ onSwitchMode, onLoginSuccess, onTotpRequired }) => {
	const [formData, setFormData] = useState({ username: "", password: "" });
	const [status, setStatus] = useState({ type: "", message: "" });
	const [isLoading, setIsLoading] = useState(false);

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsLoading(true);
		setStatus({ type: "", message: "" });

		try {
			const response = await fetch(`${API_BASE_URL}/public/login`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify(formData),
			});

			const data = await response.json();

			if (response.ok) {
				const token = data.object || data.token;
				const message = data.message || "";

				// Check for "User id: <number>" in the message to detect TOTP requirement
				const userIdMatch = message.match(/User id:\s*(\d+)/i);

				if (userIdMatch && token) {
					const userId = userIdMatch[1];
					// Pass the temp token and ID to the next step
					onTotpRequired(userId, token, formData.username);
				} else if (token) {
					// Standard login success
					localStorage.setItem("authToken", token);
					setStatus({ type: "success", message: "Login successful!" });
					onLoginSuccess(formData.username, token);
				} else {
					setStatus({
						type: "error",
						message: "Login successful but no token received.",
					});
				}
			} else {
				setStatus({
					type: "error",
					message: data.message || "Invalid credentials",
				});
			}
		} catch (error) {
			console.error("Login Error:", error);
			setStatus({ type: "error", message: "Failed to connect to server." });
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<div className="space-y-6 animate-fade-in">
			<div className="text-center space-y-2">
				<h1 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
					Welcome back
				</h1>
				<p className="text-sm text-gray-500 dark:text-gray-400">
					Enter your credentials to access your account
				</p>
			</div>

			<form onSubmit={handleSubmit} className="space-y-4">
				<InputField
					label="Username"
					icon={User}
					placeholder="johndoe"
					value={formData.username}
					onChange={(e) =>
						setFormData({ ...formData, username: e.target.value })
					}
					required
				/>
				<InputField
					label="Password"
					icon={Lock}
					type="password"
					placeholder="••••••••"
					value={formData.password}
					onChange={(e) =>
						setFormData({ ...formData, password: e.target.value })
					}
					required
				/>

				<Alert type={status.type} message={status.message} />

				<Button type="submit" isLoading={isLoading}>
					Sign In <ArrowRight size={18} />
				</Button>
			</form>

			<div className="relative">
				<div className="absolute inset-0 flex items-center">
					<div className="w-full border-t border-gray-200 dark:border-gray-700" />
				</div>
				<div className="relative flex justify-center text-sm">
					<span className="px-2 bg-white text-gray-500 dark:bg-gray-800 dark:text-gray-400">
						New here?
					</span>
				</div>
			</div>

			<Button variant="secondary" onClick={onSwitchMode}>
				Create an account
			</Button>
		</div>
	);
};

const TotpLogin = ({ userId, token, username, onVerified, onCancel }) => {
	const [otp, setOtp] = useState("");
	const [status, setStatus] = useState({ type: "", message: "" });
	const [isLoading, setIsLoading] = useState(false);

	const handleVerify = async (e) => {
		e.preventDefault();
		setIsLoading(true);
		setStatus({ type: "", message: "" });

		try {
			const response = await fetch(`${API_BASE_URL}/totp/verify`, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					Authorization: `Bearer ${token}`, // Use TEMP token here
				},
				body: JSON.stringify({
					userId: parseInt(userId),
					otp: otp,
				}),
			});

			const data = await response.json();

			if (response.ok) {
				// UPDATED LOGIC: Retrieve token from 'message' field as 'object' is null
				const finalToken = data.message || data.object;

				if (finalToken) {
					setStatus({ type: "success", message: "Verified!" });
					localStorage.setItem("authToken", finalToken);
					setTimeout(() => onVerified(username, finalToken), 500);
				} else {
					setStatus({
						type: "error",
						message: "Verified, but server did not return access token.",
					});
				}
			} else {
				setStatus({ type: "error", message: data.message || "Invalid Code." });
			}
		} catch (error) {
			console.error(error);
			setStatus({ type: "error", message: "Network error." });
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<div className="space-y-6 animate-fade-in text-center">
			<div className="flex justify-center mb-4">
				<div className="p-4 bg-indigo-100 dark:bg-indigo-900/30 rounded-full text-indigo-600 dark:text-indigo-400">
					<ShieldCheck size={32} />
				</div>
			</div>
			<h1 className="text-2xl font-bold text-gray-900 dark:text-white">
				Two-Factor Auth
			</h1>
			<p className="text-gray-500 dark:text-gray-400 text-sm">
				Enter the code from your authenticator app.
			</p>

			<form onSubmit={handleVerify} className="space-y-4">
				<InputField
					label="Authenticator Code"
					icon={Lock}
					placeholder="123456"
					value={otp}
					onChange={(e) => setOtp(e.target.value)}
					required
					maxLength={6}
					className="text-center tracking-widest font-mono text-lg"
				/>

				<Alert type={status.type} message={status.message} />

				<Button type="submit" isLoading={isLoading}>
					Verify
				</Button>
			</form>

			<button
				type="button"
				onClick={onCancel}
				className="text-sm text-gray-500 hover:text-gray-700 dark:hover:text-gray-300"
			>
				Cancel
			</button>
		</div>
	);
};

const SignupForm = ({ onSwitchMode }) => {
	const [isSuccess, setIsSuccess] = useState(false);
	const [formData, setFormData] = useState({
		userName: "",
		email: "",
		password: "",
		country: "",
	});
	const [status, setStatus] = useState({ type: "", message: "" });
	const [isLoading, setIsLoading] = useState(false);

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsLoading(true);
		setStatus({ type: "", message: "" });

		try {
			const response = await fetch(`${API_BASE_URL}/public/signup`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify(formData),
			});

			let data = {};
			const contentType = response.headers.get("content-type");
			if (contentType && contentType.includes("application/json")) {
				data = await response.json();
			}

			if (response.ok) {
				setIsSuccess(true);
				triggerVerificationEmail();
			} else {
				setStatus({
					type: "error",
					message: data.message || "Failed to create account.",
				});
			}
		} catch (error) {
			console.error("Signup Error:", error);
			setStatus({ type: "error", message: "Failed to connect to server." });
		} finally {
			setIsLoading(false);
		}
	};

	const triggerVerificationEmail = async () => {
		try {
			await fetch(`${API_BASE_URL}/public/verify`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({
					username: formData.userName,
					password: formData.password,
				}),
			});
			setStatus({ type: "success", message: "Verification email sent!" });
		} catch (e) {
			console.error("Failed to send verification email", e);
		}
	};

	if (isSuccess) {
		return (
			<div className="space-y-6 animate-fade-in text-center">
				<div className="flex justify-center mb-4">
					<div className="p-4 bg-green-100 dark:bg-green-900/30 rounded-full text-green-600 dark:text-green-400">
						<Mail size={32} />
					</div>
				</div>
				<h1 className="text-2xl font-bold text-gray-900 dark:text-white">
					Check your email
				</h1>
				<p className="text-gray-500 dark:text-gray-400">
					We've sent a verification link to <strong>{formData.email}</strong>.
				</p>

				<Alert type={status.type} message={status.message} />

				<div className="space-y-3 pt-4">
					<Button variant="secondary" onClick={triggerVerificationEmail}>
						<RefreshCw size={16} /> Resend Email
					</Button>
					<button
						type="button"
						onClick={onSwitchMode}
						className="text-indigo-600 hover:text-indigo-500 font-medium text-sm"
					>
						Back to Login
					</button>
				</div>
			</div>
		);
	}

	return (
		<div className="space-y-6 animate-fade-in">
			<div className="text-center space-y-2">
				<h1 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
					Create Account
				</h1>
				<p className="text-sm text-gray-500 dark:text-gray-400">
					Join us to manage your portfolio
				</p>
			</div>

			<form onSubmit={handleSubmit} className="space-y-4">
				<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
					<InputField
						label="Username"
						icon={User}
						placeholder="johndoe"
						value={formData.userName}
						onChange={(e) =>
							setFormData({ ...formData, userName: e.target.value })
						}
						required
					/>
					<InputField
						label="Country"
						icon={Globe}
						placeholder="USA"
						value={formData.country}
						onChange={(e) =>
							setFormData({ ...formData, country: e.target.value })
						}
					/>
				</div>

				<InputField
					label="Email Address"
					icon={Mail}
					type="email"
					placeholder="john@example.com"
					value={formData.email}
					onChange={(e) => setFormData({ ...formData, email: e.target.value })}
					required
				/>

				<InputField
					label="Password"
					icon={Lock}
					type="password"
					placeholder="Create a strong password"
					value={formData.password}
					onChange={(e) =>
						setFormData({ ...formData, password: e.target.value })
					}
					required
				/>

				<Alert type={status.type} message={status.message} />

				<Button type="submit" isLoading={isLoading}>
					Create Account
				</Button>
			</form>

			<div className="text-center text-sm">
				<span className="text-gray-500 dark:text-gray-400">
					Already have an account?{" "}
				</span>
				<button
					type="button"
					onClick={onSwitchMode}
					className="font-semibold text-indigo-600 hover:text-indigo-500 dark:text-indigo-400 dark:hover:text-indigo-300"
				>
					Sign in
				</button>
			</div>
		</div>
	);
};

const VerificationHandler = ({ token, onVerificationComplete }) => {
	const [status, setStatus] = useState({
		type: "",
		message: "Verifying your account...",
	});
	const [isVerified, setIsVerified] = useState(false);

	useEffect(() => {
		const verify = async () => {
			try {
				const response = await fetch(
					`${API_BASE_URL}/public/verification?token=${encodeURIComponent(
						token,
					)}`,
					{
						method: "GET",
						headers: { "Content-Type": "application/json" },
					},
				);

				if (response.ok) {
					setIsVerified(true);
					setStatus({
						type: "success",
						message: "Email verified successfully!",
					});
					setTimeout(() => onVerificationComplete(), 2000);
				} else {
					const data = await response.json().catch(() => ({}));
					setStatus({
						type: "error",
						message: data.message || "Invalid or expired token.",
					});
				}
			} catch (error) {
				setStatus({
					type: "error",
					message: "Verification failed. Network error.",
				});
			}
		};

		if (token) verify();
	}, [token, onVerificationComplete]);

	return (
		<div className="space-y-6 animate-fade-in text-center pt-8">
			<div className="flex justify-center mb-4">
				{isVerified ? (
					<div className="p-4 bg-green-100 dark:bg-green-900/30 rounded-full text-green-600 dark:text-green-400">
						<CheckCircle2 size={40} />
					</div>
				) : (
					<div className="p-4 bg-indigo-50 dark:bg-indigo-900/30 rounded-full text-indigo-600 dark:text-indigo-400 animate-pulse">
						<RefreshCw size={40} className="animate-spin" />
					</div>
				)}
			</div>

			<h1 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
				{isVerified ? "Verified!" : "Verifying..."}
			</h1>

			<Alert type={status.type} message={status.message} />

			<div className="pt-4">
				<Button variant="secondary" onClick={onVerificationComplete}>
					Back to Login
				</Button>
			</div>
		</div>
	);
};

window.Auth = { LoginForm, SignupForm, VerificationHandler, TotpLogin };
