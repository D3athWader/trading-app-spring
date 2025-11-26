import React from "react";
import {
	Loader2,
	CheckCircle,
	AlertCircle,
	TrendingUp,
	TrendingDown,
} from "lucide-react";

const Alert = ({ type, message }) => {
	if (!message) return null;
	const isError = type === "error";
	return (
		<div
			className={`p-4 rounded-lg flex items-start gap-3 text-sm animate-fade-in ${
				isError
					? "bg-red-50 text-red-700 border border-red-100 dark:bg-red-900/20 dark:text-red-300 dark:border-red-900/30"
					: "bg-green-50 text-green-700 border border-green-100 dark:bg-green-900/20 dark:text-green-300 dark:border-green-900/30"
			}`}
		>
			{isError ? (
				<AlertCircle size={20} className="shrink-0" />
			) : (
				<CheckCircle size={20} className="shrink-0" />
			)}
			<span>{message}</span>
		</div>
	);
};

const InputField = ({
	label,
	icon: Icon,
	type = "text",
	className = "",
	...props
}) => (
	<div className="space-y-1">
		{label && (
			<label className="block text-sm font-medium text-gray-700 dark:text-gray-300 ml-1">
				{label}
			</label>
		)}
		<div className="relative group">
			{/* CRITICAL FIX: Only render Icon if it exists to prevent crash */}
			{Icon && (
				<div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400 dark:text-gray-500 group-focus-within:text-indigo-500 dark:group-focus-within:text-indigo-400 transition-colors">
					<Icon size={18} />
				</div>
			)}
			<input
				type={type}
				className={`block w-full py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all outline-none text-gray-900 placeholder-gray-400 sm:text-sm dark:bg-gray-800 dark:border-gray-700 dark:text-gray-100 dark:placeholder-gray-500 dark:focus:ring-indigo-500/30 dark:focus:border-indigo-400 ${
					Icon ? "pl-10" : "pl-3"
				} pr-3 ${className}`}
				{...props}
			/>
		</div>
	</div>
);

const Button = ({
	children,
	isLoading,
	variant = "primary",
	className = "",
	type = "button",
	...props
}) => {
	// Added 'active:scale-95' for click effect and 'hover:-translate-y-0.5' for lift effect
	const baseStyle =
		"w-full flex justify-center items-center gap-2 py-2.5 px-4 border text-sm font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 ease-in-out active:scale-95 hover:-translate-y-0.5";
	const variants = {
		primary:
			"border-transparent text-white bg-indigo-600 hover:bg-indigo-700 focus:ring-indigo-500 shadow-lg shadow-indigo-500/30 dark:bg-indigo-500 dark:hover:bg-indigo-600 dark:shadow-indigo-900/20",
		secondary:
			"border-gray-200 text-gray-700 bg-white hover:bg-gray-50 focus:ring-gray-200 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-200 dark:hover:bg-gray-600 dark:focus:ring-gray-600",
		danger:
			"border-transparent text-white bg-red-600 hover:bg-red-700 focus:ring-red-500 shadow-lg shadow-red-500/30",
		ghost:
			"border-transparent text-indigo-600 bg-transparent hover:bg-indigo-50 focus:ring-indigo-500 dark:text-indigo-400 dark:hover:bg-indigo-900/20",
	};

	return (
		<button
			type={type}
			className={`${baseStyle} ${variants[variant]} ${className}`}
			disabled={isLoading}
			{...props}
		>
			{isLoading && <Loader2 size={18} className="animate-spin" />}
			{children}
		</button>
	);
};

const StatCard = ({ title, value, icon: Icon, trend }) => (
	// Added hover effect to StatCard as well
	<div className="bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-1">
		<div className="flex justify-between items-start mb-4">
			<div className="p-3 bg-indigo-50 dark:bg-indigo-900/30 rounded-xl text-indigo-600 dark:text-indigo-400">
				{Icon && <Icon size={24} />}
			</div>
			{trend !== undefined && (
				<span
					className={`flex items-center text-xs font-medium px-2.5 py-0.5 rounded-full ${
						trend > 0
							? "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400"
							: "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400"
					}`}
				>
					{trend > 0 ? (
						<TrendingUp size={12} className="mr-1" />
					) : (
						<TrendingDown size={12} className="mr-1" />
					)}
					{Math.abs(trend)}%
				</span>
			)}
		</div>
		<h3 className="text-gray-500 dark:text-gray-400 text-sm font-medium">
			{title}
		</h3>
		<p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
			{value}
		</p>
	</div>
);

// Expose components to window for other files to use
window.UI = { Alert, InputField, Button, StatCard };
