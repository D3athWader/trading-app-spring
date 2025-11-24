import React, { useState, useEffect, useRef } from "react";
import {
  LayoutDashboard,
  LineChart,
  PieChart,
  LogOut,
  Wallet,
  DollarSign,
  Bell,
  Search,
  Plus,
  Minus,
  RefreshCw,
  Settings,
  Sun,
  Moon,
  ShieldCheck,
  User as UserIcon,
  Lock,
  CheckCircle,
  Briefcase,
  Users,
  Trash2,
  TrendingUp,
  History,
  ShoppingCart,
  XCircle,
  Activity,
  ArrowUpRight,
  ArrowDownLeft,
  Zap,
  Wifi,
  WifiOff,
} from "lucide-react";

// CRITICAL FIX: Robustly handle AppConfig
const AppConfig = window.AppConfig || { API_BASE_URL: "" };
const { API_BASE_URL } = AppConfig;

// SAFELY destructure window.UI
const UI = window.UI || {};
const Button =
  UI.Button ||
  (({ children, ...props }) => <button {...props}>{children}</button>);
const Alert = UI.Alert || (() => null);
const StatCard = UI.StatCard || (() => <div />);
const InputField = UI.InputField || (({ ...props }) => <input {...props} />);

// --- Utility Functions ---

const formatDateTime = (timestamp, onlyTime = false) => {
  if (!timestamp) return "--";
  try {
    let date;
    if (Array.isArray(timestamp)) {
      date = new Date(
        timestamp[0],
        timestamp[1] - 1,
        timestamp[2],
        timestamp[3],
        timestamp[4],
        timestamp[5] || 0,
      );
    } else {
      date = new Date(timestamp);
    }

    if (isNaN(date.getTime())) return "Invalid Date";

    if (onlyTime) {
      return date.toLocaleTimeString([], { hour12: false });
    }
    return date.toLocaleString();
  } catch (e) {
    console.error("Date parsing error", e);
    return "--";
  }
};

// --- Helper Components ---

const TradeModal = ({
  isOpen,
  onClose,
  type,
  stockSymbol,
  currentPrice,
  token,
  username,
  onTradeComplete,
}) => {
  const [quantity, setQuantity] = useState(1);
  const [customPrice, setCustomPrice] = useState(currentPrice || 0);
  const [isLoading, setIsLoading] = useState(false);
  const [status, setStatus] = useState(null);
  const [orderType, setOrderType] = useState(type || "BUY");

  useEffect(() => {
    setCustomPrice(currentPrice || 0);
    setOrderType(type || "BUY");
  }, [currentPrice, stockSymbol, isOpen, type]);

  if (!isOpen) return null;

  const handleTrade = async (e) => {
    if (e) e.preventDefault();

    if (quantity <= 0 || customPrice <= 0) {
      setStatus({ type: "error", message: "Invalid quantity or price." });
      return;
    }

    setIsLoading(true);
    setStatus(null);
    try {
      const endpoint =
        orderType === "BUY" ? "/order/buy-order" : "/order/sell-order";
      const payload = {
        stockSymbol,
        quantity: parseInt(quantity),
        price: parseFloat(customPrice),
        type: orderType,
        username: username,
      };

      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        setStatus({
          type: "success",
          message: `Order to ${orderType} ${quantity} ${stockSymbol} at $${customPrice} placed!`,
        });
        setTimeout(() => {
          onTradeComplete();
          onClose();
        }, 1500);
      } else {
        const data = await response.json();
        setStatus({
          type: "error",
          message: data.message || "Trade failed. Check balance/holdings.",
        });
      }
    } catch (e) {
      console.error("Trade Error:", e);
      setStatus({ type: "error", message: "Network error occurred." });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-fade-in">
      <div className="bg-white dark:bg-gray-800 rounded-2xl w-full max-w-sm p-6 shadow-2xl relative transition-all">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
        >
          <XCircle size={20} />
        </button>

        <div className="flex p-1 mb-6 bg-gray-100 dark:bg-gray-700 rounded-xl">
          <button
            type="button"
            onClick={() => setOrderType("BUY")}
            className={`flex-1 py-2 text-sm font-bold rounded-lg transition-all duration-200 ${
              orderType === "BUY"
                ? "bg-white dark:bg-gray-600 text-green-600 dark:text-green-400 shadow-sm scale-105"
                : "text-gray-500 dark:text-gray-400 hover:text-gray-700"
            }`}
          >
            Buy
          </button>
          <button
            type="button"
            onClick={() => setOrderType("SELL")}
            className={`flex-1 py-2 text-sm font-bold rounded-lg transition-all duration-200 ${
              orderType === "SELL"
                ? "bg-white dark:bg-gray-600 text-red-600 dark:text-red-400 shadow-sm scale-105"
                : "text-gray-500 dark:text-gray-400 hover:text-gray-700"
            }`}
          >
            Sell
          </button>
        </div>

        <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4 text-center">
          {orderType === "BUY" ? "Buy" : "Sell"}{" "}
          <span className="text-indigo-600 dark:text-indigo-400">
            {stockSymbol}
          </span>
        </h3>

        <div className="space-y-4 mb-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Order Price ($)
            </label>
            <input
              type="number"
              step="0.01"
              min="0.01"
              value={customPrice}
              onChange={(e) => setCustomPrice(e.target.value)}
              className="block w-full py-2 px-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 outline-none text-gray-900 dark:bg-gray-700 dark:border-gray-600 dark:text-white transition-all"
            />
            <p className="text-xs text-gray-500 mt-1">
              Current Market Price: $
              {currentPrice ? currentPrice.toFixed(2) : "0.00"}
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Quantity
            </label>
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={() => setQuantity(Math.max(1, quantity - 1))}
                className="p-2 rounded-lg border border-gray-200 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-300"
              >
                <Minus size={16} />
              </button>
              <input
                type="number"
                min="1"
                value={quantity}
                onChange={(e) => setQuantity(parseInt(e.target.value) || 0)}
                className="flex-1 text-center py-2 bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg text-gray-900 dark:text-white"
              />
              <button
                type="button"
                onClick={() => setQuantity(quantity + 1)}
                className="p-2 rounded-lg border border-gray-200 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-300"
              >
                <Plus size={16} />
              </button>
            </div>
          </div>

          <div className="flex justify-between text-sm font-bold pt-4 border-t border-gray-100 dark:border-gray-700">
            <span className="text-gray-900 dark:text-white">Total Value</span>
            <span className="text-indigo-600 dark:text-indigo-400">
              ${(customPrice * quantity).toFixed(2)}
            </span>
          </div>
        </div>

        <Alert
          type={status && status.type}
          message={status && status.message}
        />

        <div className="flex gap-3 mt-4">
          <Button variant="secondary" onClick={onClose} className="flex-1">
            Cancel
          </Button>
          <Button
            variant={orderType === "BUY" ? "primary" : "danger"}
            className="flex-1"
            isLoading={isLoading}
            onClick={handleTrade}
          >
            Confirm {orderType}
          </Button>
        </div>
      </div>
    </div>
  );
};

const TradesList = ({ user, token }) => {
  const [historyTrades, setHistoryTrades] = useState([]);
  const [loadingHistory, setLoadingHistory] = useState(true);

  useEffect(() => {
    const fetchHistory = async () => {
      if (!token) return;
      try {
        const res = await fetch(`${API_BASE_URL}/trade/history`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (res.ok) {
          const data = await res.json();
          setHistoryTrades(data.object || []);
        }
      } catch (e) {
        console.error("Failed to fetch trade history", e);
      } finally {
        setLoadingHistory(false);
      }
    };
    fetchHistory();
  }, [token]);

  return (
    <div className="h-full flex flex-col">
      {loadingHistory ? (
        <div className="flex items-center justify-center flex-1">
          <RefreshCw size={24} className="animate-spin text-gray-400" />
        </div>
      ) : historyTrades.length === 0 ? (
        <div className="p-8 text-center text-gray-500 dark:text-gray-400 flex-1">
          No personal trades found.
        </div>
      ) : (
        <div className="overflow-auto flex-1">
          <table className="w-full text-left">
            <thead className="bg-gray-50 dark:bg-gray-900/50 sticky top-0 backdrop-blur-sm">
              <tr className="text-xs uppercase text-gray-500 dark:text-gray-400">
                <th className="px-6 py-3">Date</th>
                <th className="px-6 py-3">Action</th>
                <th className="px-6 py-3">Symbol</th>
                <th className="px-6 py-3 text-right">Price</th>
                <th className="px-6 py-3 text-right">Total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
              {historyTrades.map((trade) => {
                const isBuyer = trade.buyerUsername === user?.username;
                return (
                  <tr
                    key={trade.id}
                    className="hover:bg-gray-50 dark:hover:bg-gray-700/20"
                  >
                    <td className="px-6 py-3 text-xs text-gray-500 whitespace-nowrap">
                      {formatDateTime(trade.timestamp)}
                    </td>
                    <td className="px-6 py-3">
                      <span
                        className={`inline-flex items-center gap-1 px-2 py-1 rounded text-xs font-bold ${
                          isBuyer
                            ? "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400"
                            : "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400"
                        }`}
                      >
                        {isBuyer ? (
                          <ArrowDownLeft size={12} />
                        ) : (
                          <ArrowUpRight size={12} />
                        )}
                        {isBuyer ? "BOUGHT" : "SOLD"}
                      </span>
                    </td>
                    <td className="px-6 py-3 font-bold text-gray-900 dark:text-white">
                      {trade.stockSymbol}
                    </td>
                    <td className="px-6 py-3 text-right text-gray-600 dark:text-gray-300">
                      ${trade.price?.toFixed(2)}
                    </td>
                    <td className="px-6 py-3 text-right font-bold text-gray-900 dark:text-white">
                      ${(trade.price * trade.quantity).toFixed(2)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

// --- Reusable Live Market Widget ---
const LiveMarketWidget = ({ liveTrades, connectionStatus }) => {
  return (
    <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden flex flex-col h-full min-h-[400px]">
      <div className="p-6 border-b border-gray-100 dark:border-gray-700 flex items-center justify-between bg-white dark:bg-gray-800 sticky top-0 z-10">
        <div className="flex items-center gap-3">
          <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
            <Activity size={20} className="text-green-500" /> Live Feed
          </h3>
          {/* Connection Status Indicator */}
          {connectionStatus === "connected" ? (
            <span className="flex items-center gap-1 px-2 py-0.5 rounded-full bg-green-100 text-green-700 text-xs font-medium dark:bg-green-900/30 dark:text-green-400">
              <Wifi size={12} /> Live
            </span>
          ) : (
            <span className="flex items-center gap-1 px-2 py-0.5 rounded-full bg-red-100 text-red-700 text-xs font-medium dark:bg-red-900/30 dark:text-red-400">
              <WifiOff size={12} /> Disconnected
            </span>
          )}
        </div>
        {connectionStatus === "connected" && (
          <span className="flex h-2 w-2 relative">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
          </span>
        )}
      </div>
      <div className="overflow-auto flex-1 relative">
        {liveTrades.length === 0 ? (
          <div className="absolute inset-0 flex flex-col items-center justify-center text-gray-400">
            <RefreshCw
              size={32}
              className={`mb-2 opacity-50 ${
                connectionStatus === "connected" ? "animate-spin" : ""
              }`}
            />
            <p>
              {connectionStatus === "connected"
                ? "Waiting for trades..."
                : "Connecting to market..."}
            </p>
          </div>
        ) : (
          <table className="w-full text-left">
            <thead className="bg-gray-50 dark:bg-gray-900/50 sticky top-0 backdrop-blur-sm z-10">
              <tr className="text-xs uppercase text-gray-500 dark:text-gray-400">
                <th className="px-6 py-3">Time</th>
                <th className="px-6 py-3">Symbol</th>
                <th className="px-6 py-3 text-right">Price</th>
                <th className="px-6 py-3 text-right">Qty</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
              {liveTrades.map((trade, idx) => (
                <tr
                  key={trade.id || idx}
                  className="animate-fade-in hover:bg-gray-50 dark:hover:bg-gray-700/20 transition-colors"
                >
                  <td className="px-6 py-3 text-sm text-gray-500 font-mono whitespace-nowrap">
                    {formatDateTime(trade.timestamp, true)}
                  </td>
                  <td className="px-6 py-3 font-bold text-gray-900 dark:text-white">
                    {trade.stockSymbol}
                  </td>
                  <td className="px-6 py-3 text-right font-medium text-indigo-600 dark:text-indigo-400">
                    ${trade.price?.toFixed(2)}
                  </td>
                  <td className="px-6 py-3 text-right text-gray-600 dark:text-gray-300">
                    {trade.quantity}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

// --- Floating Theme Button ---
const FloatingThemeToggle = ({ isDarkMode, toggleTheme }) => {
  return (
    <button
      onClick={toggleTheme}
      className="fixed bottom-6 right-6 z-50 w-14 h-14 rounded-full bg-indigo-600 dark:bg-indigo-500 text-white shadow-xl hover:shadow-2xl hover:scale-110 active:scale-90 transition-all duration-300 ease-out group flex items-center justify-center"
      aria-label="Toggle Theme"
    >
      <div className="relative w-6 h-6 flex items-center justify-center">
        <Sun
          className={`absolute transition-all duration-500 ${
            isDarkMode
              ? "opacity-0 rotate-90 scale-0"
              : "opacity-100 rotate-0 scale-100"
          }`}
          size={24}
        />
        <Moon
          className={`absolute transition-all duration-500 ${
            isDarkMode
              ? "opacity-100 rotate-0 scale-100"
              : "opacity-0 -rotate-90 scale-0"
          }`}
          size={24}
        />
      </div>
      <span className="absolute right-full mr-3 top-1/2 -translate-y-1/2 px-2 py-1 bg-gray-900 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none shadow-lg">
        {isDarkMode ? "Light Mode" : "Dark Mode"}
      </span>
    </button>
  );
};

// --- Settings Panel (FIXED: Token Checks) ---
const SettingsPanel = ({ user, token, onUpdate }) => {
  const [amount, setAmount] = useState("");
  const [balanceStatus, setBalanceStatus] = useState(null);
  const [totpStatus, setTotpStatus] = useState(null);
  const [loadingBalance, setLoadingBalance] = useState(false);
  const [loadingTotp, setLoadingTotp] = useState(false);

  const [tempTotpToken, setTempTotpToken] = useState(null);
  const [qrCode, setQrCode] = useState(null);
  const [otpInput, setOtpInput] = useState("");
  const [isTotpEnabled, setIsTotpEnabled] = useState(false);

  useEffect(() => {
    const checkTotpStatus = async () => {
      if (!token) return;
      try {
        const response = await fetch(`${API_BASE_URL}/user-panel/totp-status`, {
          method: "GET",
          headers: { Authorization: `Bearer ${token}` },
        });
        if (response.ok) {
          const data = await response.json();
          setIsTotpEnabled(data.object === true);
        }
      } catch (e) {
        console.error("Failed to check TOTP status", e);
      }
    };
    checkTotpStatus();
  }, [token]);

  const handleAddBalance = async (e) => {
    e.preventDefault();
    if (!amount || isNaN(amount) || amount <= 0) return;
    if (!token) {
      setBalanceStatus({ type: "error", message: "Authentication required." });
      return;
    }

    setLoadingBalance(true);
    setBalanceStatus(null);
    try {
      const response = await fetch(
        `${API_BASE_URL}/user-panel/add-balance?balance=${amount}`,
        {
          method: "GET",
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      if (response.ok) {
        setBalanceStatus({
          type: "success",
          message: `Successfully added $${amount}`,
        });
        setAmount("");
        onUpdate();
      } else {
        setBalanceStatus({ type: "error", message: "Failed to add balance." });
      }
    } catch (err) {
      setBalanceStatus({ type: "error", message: "Network error." });
    } finally {
      setLoadingBalance(false);
    }
  };

  const handleInitTotp = async () => {
    if (!token) {
      setTotpStatus({ type: "error", message: "Authentication required." });
      return;
    }
    setLoadingTotp(true);
    setTotpStatus(null);
    try {
      const response = await fetch(`${API_BASE_URL}/user-panel/enable-totp`, {
        method: "GET",
        headers: { Authorization: `Bearer ${token}` },
      });

      const data = await response.json();
      if (response.ok) {
        setQrCode(data.message);
        setTempTotpToken(data.object);
        setTotpStatus({
          type: "success",
          message: "Scan the QR Code and enter the code below.",
        });
      } else {
        setTotpStatus({ type: "error", message: "Failed to fetch QR Code." });
      }
    } catch (err) {
      setTotpStatus({ type: "error", message: "Network error." });
    } finally {
      setLoadingTotp(false);
    }
  };

  const handleConfirmTotp = async (e) => {
    e.preventDefault();
    if (!otpInput || otpInput.length < 6) return;
    if (!user || !user.id) {
      setTotpStatus({
        type: "error",
        message: "User ID missing. Please refresh.",
      });
      return;
    }
    if (!tempTotpToken) {
      setTotpStatus({
        type: "error",
        message: "Session expired. Please try enabling again.",
      });
      return;
    }

    setLoadingTotp(true);
    try {
      const response = await fetch(`${API_BASE_URL}/totp/setup`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${tempTotpToken}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          userId: user.id,
          otp: otpInput,
        }),
      });

      const data = await response.json();
      if (response.ok && data.object === true) {
        setTotpStatus({
          type: "success",
          message: "TOTP Enabled Successfully!",
        });
        setQrCode(null);
        setOtpInput("");
        setTempTotpToken(null);
        setIsTotpEnabled(true);
        onUpdate();
      } else {
        setTotpStatus({ type: "error", message: "Invalid Code. Try again." });
      }
    } catch (err) {
      setTotpStatus({ type: "error", message: "Verification failed." });
    } finally {
      setLoadingTotp(false);
    }
  };

  const getQrImageSrc = (code) => {
    if (!code) return "";
    let src = String(code);
    if (src.indexOf("%") > -1) {
      try {
        src = decodeURIComponent(src);
      } catch (e) {}
    }
    if (src.startsWith("data:") || src.startsWith("http")) {
      return src;
    }
    return `data:image/png;base64,${src}`;
  };

  // Safety check for user object to prevent white screen
  if (!user) {
    return (
      <div className="text-gray-500 text-center p-10">Loading user data...</div>
    );
  }

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
        User Panel
      </h2>

      <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm">
        <div className="flex items-center gap-4 mb-4">
          <div className="p-3 bg-indigo-50 dark:bg-indigo-900/30 rounded-xl text-indigo-600 dark:text-indigo-400">
            <UserIcon size={24} />
          </div>
          <div>
            <h3 className="text-lg font-bold text-gray-900 dark:text-white">
              Profile Details
            </h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Your account information
            </p>
          </div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">
              Status
            </label>
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400">
              Active
            </span>
          </div>
          <div>
            <label className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">
              TOTP
            </label>
            <span
              className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                isTotpEnabled
                  ? "bg-green-100 text-green-800"
                  : "bg-gray-100 text-gray-800"
              }`}
            >
              {isTotpEnabled ? "Enabled" : "Disabled"}
            </span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm">
          <div className="flex items-center gap-4 mb-4">
            <div className="p-3 bg-green-50 dark:bg-green-900/30 rounded-xl text-green-600 dark:text-green-400">
              <Wallet size={24} />
            </div>
            <div>
              <h3 className="text-lg font-bold text-gray-900 dark:text-white">
                Wallet
              </h3>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Top up your balance
              </p>
            </div>
          </div>

          <form onSubmit={handleAddBalance} className="space-y-4">
            <InputField
              label="Amount ($)"
              icon={DollarSign || undefined}
              type="number"
              placeholder="0.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              min="1"
            />
            <Alert
              type={balanceStatus && balanceStatus.type}
              message={balanceStatus && balanceStatus.message}
            />
            <Button type="submit" isLoading={loadingBalance}>
              Add Balance
            </Button>
          </form>
        </div>

        <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm">
          <div className="flex items-center gap-4 mb-4">
            <div
              className={`p-3 rounded-xl ${
                isTotpEnabled
                  ? "bg-green-50 dark:bg-green-900/30 text-green-600 dark:text-green-400"
                  : "bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400"
              }`}
            >
              <ShieldCheck size={24} />
            </div>
            <div>
              <h3 className="text-lg font-bold text-gray-900 dark:text-white">
                Security
              </h3>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Two-Factor Authentication
              </p>
            </div>
          </div>

          <div className="space-y-4">
            {isTotpEnabled ? (
              <div className="flex flex-col items-center justify-center py-6 text-center space-y-3">
                <div className="p-4 bg-green-100 dark:bg-green-900/20 rounded-full">
                  <CheckCircle
                    size={48}
                    className="text-green-600 dark:text-green-400"
                  />
                </div>
                <div>
                  <h4 className="font-semibold text-gray-900 dark:text-white">
                    TOTP is Active
                  </h4>
                  <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                    Your account is secured with two-factor authentication.
                  </p>
                </div>
              </div>
            ) : (
              <>
                {!qrCode ? (
                  <>
                    <p className="text-sm text-gray-600 dark:text-gray-300 leading-relaxed">
                      Protect your account by enabling Time-based One-Time
                      Password (TOTP) using Google Authenticator or Authy.
                    </p>
                    <Button
                      variant="secondary"
                      onClick={handleInitTotp}
                      isLoading={loadingTotp}
                    >
                      Enable TOTP
                    </Button>
                  </>
                ) : (
                  <div className="space-y-4 animate-fade-in">
                    <div className="flex justify-center p-4 bg-white rounded-xl border border-gray-200">
                      <img
                        src={getQrImageSrc(qrCode)}
                        alt="TOTP QR Code"
                        className="w-40 h-40 object-contain"
                      />
                    </div>
                    <p className="text-xs text-center text-gray-500">
                      Scan this code with your app
                    </p>

                    <form onSubmit={handleConfirmTotp} className="space-y-3">
                      <InputField
                        icon={Lock}
                        placeholder="Enter 6-digit code"
                        value={otpInput}
                        onChange={(e) => setOtpInput(e.target.value)}
                        className="text-center tracking-widest"
                        maxLength={6}
                      />
                      <Button type="submit" isLoading={loadingTotp}>
                        Verify & Enable
                      </Button>
                    </form>
                  </div>
                )}
                <Alert
                  type={totpStatus && totpStatus.type}
                  message={totpStatus && totpStatus.message}
                />
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// --- Admin Panel Component ---
const AdminPanel = ({ token }) => {
  const [activeSection, setActiveSection] = useState("companies");
  const [status, setStatus] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // Data States
  const [users, setUsers] = useState([]);
  const [stocks, setStocks] = useState([]);

  // Form States
  const [companyForm, setCompanyForm] = useState({
    name: "",
    tickerSymbol: "",
    sector: "",
  });
  const [stockForm, setStockForm] = useState({
    symbol: "",
    openPrice: "",
    totalStocks: "",
    companyId: "",
  });
  const [deleteCompanyId, setDeleteCompanyId] = useState("");

  useEffect(() => {
    if (activeSection === "users") fetchUsers();
    if (activeSection === "stocks") fetchStocks();
  }, [activeSection]);

  const fetchUsers = async () => {
    setIsLoading(true);
    try {
      const res = await fetch(`${API_BASE_URL}/user-panel/all-users`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        const data = await res.json();
        setUsers(data.object || []);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchStocks = async () => {
    setIsLoading(true);
    try {
      const res = await fetch(`${API_BASE_URL}/stock/search`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        const data = await res.json();
        setStocks(data.object || []);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateCompany = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setStatus(null);
    try {
      const res = await fetch(`${API_BASE_URL}/admin/company/create-company`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(companyForm),
      });
      const data = await res.json();
      if (res.ok) {
        setStatus({
          type: "success",
          message: `Company created! ID: ${data.object.id}`,
        });
        setCompanyForm({ name: "", tickerSymbol: "", sector: "" });
      } else {
        setStatus({ type: "error", message: data.message || "Failed" });
      }
    } catch (e) {
      setStatus({ type: "error", message: "Network Error" });
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteCompany = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setStatus(null);
    try {
      const res = await fetch(
        `${API_BASE_URL}/admin/company/delete/${deleteCompanyId}`,
        {
          method: "DELETE",
          headers: { Authorization: `Bearer ${token}` },
        },
      );
      if (res.ok) {
        setStatus({ type: "success", message: "Company deleted successfully" });
        setDeleteCompanyId("");
      } else {
        setStatus({ type: "error", message: "Failed to delete (check ID)" });
      }
    } catch (e) {
      setStatus({ type: "error", message: "Network Error" });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateStock = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setStatus(null);
    try {
      const res = await fetch(`${API_BASE_URL}/admin/stock/new-stock`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          ...stockForm,
          companyId: parseInt(stockForm.companyId),
          openPrice: parseFloat(stockForm.openPrice),
          totalStocks: parseInt(stockForm.totalStocks),
        }),
      });
      const data = await res.json();
      if (res.ok) {
        setStatus({ type: "success", message: "Stock created successfully!" });
        setStockForm({
          symbol: "",
          openPrice: "",
          totalStocks: "",
          companyId: "",
        });
        fetchStocks();
      } else {
        setStatus({ type: "error", message: data.message || "Failed" });
      }
    } catch (e) {
      setStatus({ type: "error", message: "Network Error" });
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteStock = async (id) => {
    if (!confirm("Are you sure?")) return;
    try {
      const res = await fetch(
        `${API_BASE_URL}/admin/stock/delete-stock/${id}`,
        {
          method: "DELETE",
          headers: { Authorization: `Bearer ${token}` },
        },
      );
      if (res.ok) {
        fetchStocks();
        setStatus({ type: "success", message: "Stock deleted" });
      } else {
        setStatus({ type: "error", message: "Failed to delete stock" });
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleMakeAdmin = async (username) => {
    try {
      const res = await fetch(`${API_BASE_URL}/admin/make-admin/${username}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        setStatus({
          type: "success",
          message: `User ${username} is now Admin`,
        });
        fetchUsers();
      } else {
        setStatus({ type: "error", message: "Failed to promote user" });
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
          <ShieldCheck className="text-indigo-600" /> Admin Panel
        </h2>
        <div className="flex bg-gray-100 dark:bg-gray-700 p-1 rounded-lg">
          {["companies", "stocks", "users"].map((sec) => (
            <button
              key={sec}
              onClick={() => {
                setActiveSection(sec);
                setStatus(null);
              }}
              className={`px-4 py-2 rounded-md text-sm font-medium capitalize transition-all ${
                activeSection === sec
                  ? "bg-white dark:bg-gray-600 text-indigo-600 dark:text-white shadow-sm"
                  : "text-gray-500 dark:text-gray-300 hover:text-gray-700"
              }`}
            >
              {sec}
            </button>
          ))}
        </div>
      </div>

      <Alert type={status?.type} message={status?.message} />

      {activeSection === "companies" && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
              <Briefcase size={20} /> Create Company
            </h3>
            <form onSubmit={handleCreateCompany} className="space-y-4">
              <InputField
                label="Name"
                value={companyForm.name}
                onChange={(e) =>
                  setCompanyForm({ ...companyForm, name: e.target.value })
                }
                placeholder="Tech Corp"
                required
              />
              <InputField
                label="Ticker Symbol"
                value={companyForm.tickerSymbol}
                onChange={(e) =>
                  setCompanyForm({
                    ...companyForm,
                    tickerSymbol: e.target.value,
                  })
                }
                placeholder="TCP"
                required
              />
              <InputField
                label="Sector"
                value={companyForm.sector}
                onChange={(e) =>
                  setCompanyForm({ ...companyForm, sector: e.target.value })
                }
                placeholder="Technology"
                required
              />
              <Button type="submit" isLoading={isLoading}>
                Create Company
              </Button>
            </form>
          </div>

          <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm h-fit">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
              <Trash2 size={20} className="text-red-500" /> Delete Company
            </h3>
            <form onSubmit={handleDeleteCompany} className="space-y-4">
              <InputField
                label="Company ID"
                type="number"
                value={deleteCompanyId}
                onChange={(e) => setDeleteCompanyId(e.target.value)}
                placeholder="ID (e.g., 1)"
                required
              />
              <p className="text-xs text-gray-500">
                Warning: This action cannot be undone.
              </p>
              <Button type="submit" variant="danger" isLoading={isLoading}>
                Delete Company
              </Button>
            </form>
          </div>
        </div>
      )}

      {activeSection === "stocks" && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-1 bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm h-fit">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
              <TrendingUp size={20} /> Add New Stock
            </h3>
            <form onSubmit={handleCreateStock} className="space-y-3">
              <InputField
                label="Symbol"
                value={stockForm.symbol}
                onChange={(e) =>
                  setStockForm({ ...stockForm, symbol: e.target.value })
                }
                placeholder="AAPL"
                required
              />
              <div className="grid grid-cols-2 gap-2">
                <InputField
                  label="Open Price"
                  type="number"
                  value={stockForm.openPrice}
                  onChange={(e) =>
                    setStockForm({ ...stockForm, openPrice: e.target.value })
                  }
                  required
                />
                <InputField
                  label="Total Stocks"
                  type="number"
                  value={stockForm.totalStocks}
                  onChange={(e) =>
                    setStockForm({ ...stockForm, totalStocks: e.target.value })
                  }
                  required
                />
              </div>
              <InputField
                label="Company ID"
                type="number"
                value={stockForm.companyId}
                onChange={(e) =>
                  setStockForm({ ...stockForm, companyId: e.target.value })
                }
                placeholder="Associated Company ID"
                required
              />
              <Button type="submit" isLoading={isLoading}>
                Create Stock
              </Button>
            </form>
          </div>

          <div className="lg:col-span-2 bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4">
              Existing Stocks
            </h3>
            <div className="overflow-x-auto max-h-96 overflow-y-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-50 dark:bg-gray-700 text-xs uppercase text-gray-500 dark:text-gray-300">
                    <th className="p-3">ID</th>
                    <th className="p-3">Symbol</th>
                    <th className="p-3">Price</th>
                    <th className="p-3 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                  {stocks.map((s) => (
                    <tr key={s.id}>
                      <td className="p-3 text-sm text-gray-500">{s.id}</td>
                      <td className="p-3 font-bold text-gray-900 dark:text-white">
                        {s.symbol}
                      </td>
                      <td className="p-3 text-sm">${s.currentPrice}</td>
                      <td className="p-3 text-right">
                        <button
                          onClick={() => handleDeleteStock(s.id)}
                          className="text-red-500 hover:text-red-700 text-sm font-medium"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {activeSection === "users" && (
        <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden">
          <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
            <Users size={20} /> User Management
          </h3>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 dark:bg-gray-700 text-xs uppercase text-gray-500 dark:text-gray-300">
                  <th className="p-4">ID</th>
                  <th className="p-4">Username</th>
                  <th className="p-4">Roles</th>
                  <th className="p-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                {users.map((u) => (
                  <tr
                    key={u.id}
                    className="hover:bg-gray-50 dark:hover:bg-gray-700/20"
                  >
                    <td className="p-4 text-sm text-gray-500">{u.id}</td>
                    <td className="p-4 font-medium text-gray-900 dark:text-white">
                      {u.userName}
                    </td>
                    <td className="p-4 text-sm">
                      {u.role?.map((r) => (
                        <span
                          key={r}
                          className="inline-block mr-1 px-2 py-0.5 bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300 rounded-full text-xs"
                        >
                          {r.replace("ROLE_", "")}
                        </span>
                      ))}
                    </td>
                    <td className="p-4 text-right">
                      {!u.role?.includes("ADMIN") && (
                        <button
                          onClick={() => handleMakeAdmin(u.userName)}
                          className="text-indigo-600 hover:text-indigo-800 text-sm font-medium"
                        >
                          Make Admin
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

// --- Main Dashboard Component ---

const Dashboard = ({ user, token, onLogout, isDarkMode, toggleTheme }) => {
  const [fullUser, setFullUser] = useState(user);
  const [balance, setBalance] = useState(0);
  const [totalAssets, setTotalAssets] = useState(0);
  const [stocks, setStocks] = useState([]);
  const [holdings, setHoldings] = useState([]);
  const [activeOrdersCount, setActiveOrdersCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("market");
  const [userOrders, setUserOrders] = useState([]);

  // WebSocket State
  const [liveTrades, setLiveTrades] = useState([]);
  const [connectionStatus, setConnectionStatus] = useState("disconnected");
  const stompClientRef = useRef(null);

  // Admin State
  const [isAdmin, setIsAdmin] = useState(false);

  const [tradeModal, setTradeModal] = useState({
    isOpen: false,
    type: "BUY",
    stock: null,
  });

  // Initial Fetch
  useEffect(() => {
    const checkAdminStatus = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/admin/check`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (res.ok) setIsAdmin(true);
      } catch (e) {
        console.log("Not an admin");
      }
    };
    if (token) {
      checkAdminStatus();
      fetchData();
    }
  }, [token]);

  // WebSocket Connection Logic
  useEffect(() => {
    let retryCount = 0;
    const maxRetries = 10; // Try for 10 seconds

    const connectWebSocket = () => {
      // Check if libraries are available on window object
      if (
        typeof window.SockJS === "undefined" ||
        typeof window.Stomp === "undefined"
      ) {
        console.warn("WebSocket libraries not ready yet. Retrying...");
        if (retryCount < maxRetries) {
          retryCount++;
          setTimeout(connectWebSocket, 1000);
        } else {
          console.error("Failed to load WebSocket libraries.");
        }
        return;
      }

      // Prevent multiple connections
      if (stompClientRef.current && stompClientRef.current.connected) return;

      try {
        const socket = new window.SockJS(`${API_BASE_URL}/ws`);
        const stompClient = window.Stomp.over(socket);
        stompClientRef.current = stompClient;

        // Disable debug output
        stompClient.debug = () => {};

        stompClient.connect(
          {
            // Pass auth token in header
            Authorization: `Bearer ${token}`,
          },
          (frame) => {
            setConnectionStatus("connected");
            console.log("Connected to WebSocket");

            stompClient.subscribe("/topic/trades", (message) => {
              try {
                if (message.body) {
                  const trade = JSON.parse(message.body);
                  setLiveTrades((prev) => [trade, ...prev].slice(0, 20));
                }
              } catch (e) {
                console.error("Error parsing trade message", e);
              }
            });
          },
          (error) => {
            console.error("WebSocket Connection Error:", error);
            setConnectionStatus("disconnected");
            // Simple reconnection logic after 5s
            setTimeout(connectWebSocket, 5000);
          },
        );
      } catch (e) {
        console.error("WebSocket init error:", e);
        setConnectionStatus("error");
      }
    };

    if (token) {
      connectWebSocket();
    }

    return () => {
      if (stompClientRef.current) {
        try {
          stompClientRef.current.disconnect(() => {
            console.log("Disconnected from WebSocket");
          });
        } catch (e) {
          // Ignore errors during disconnect
        }
      }
    };
  }, [token]); // Depend on token to reconnect if it changes

  const fetchData = async () => {
    if (!token) return;
    setLoading(true);
    try {
      let currentBalance = 0;

      if (user && user.username) {
        const userRes = await fetch(
          `${API_BASE_URL}/user-panel/find-user/${user.username}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          },
        );
        if (userRes.ok) {
          const userData = await userRes.json();
          setFullUser(userData.object);
        }
      }

      const balanceRes = await fetch(`${API_BASE_URL}/user-panel/get-balance`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (balanceRes.ok) {
        const balanceData = await balanceRes.json();
        currentBalance = balanceData.object || 0;
        setBalance(currentBalance);
      }

      // Market Data
      if (activeTab === "market" || activeTab === "orders") {
        const stockRes = await fetch(`${API_BASE_URL}/stock/search`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (stockRes.ok) {
          const stockData = await stockRes.json();
          setStocks(stockData.object || []);
        }
      }

      if (user && user.username) {
        const ordersRes = await fetch(
          `${API_BASE_URL}/order/userName/${user.username}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          },
        );
        if (ordersRes.ok) {
          const ordersData = await ordersRes.json();
          const orders = ordersData.object || [];
          setUserOrders(orders);
          const activeCount = orders.filter(
            (o) => o.status === "PENDING" || o.status === "PARTIALLY_FILLED",
          ).length;
          setActiveOrdersCount(activeCount);
        }
      }

      const portfolioRes = await fetch(
        `${API_BASE_URL}/portfolio/get-portfolio`,
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      if (portfolioRes.ok) {
        const portData = await portfolioRes.json();
        const portfolioList = portData.object || [];
        let stockValue = 0;

        if (Array.isArray(portfolioList)) {
          setHoldings(portfolioList);
          stockValue = portfolioList.reduce((acc, item) => {
            return acc + (item.totalValue || 0);
          }, 0);
        } else {
          setHoldings([]);
        }
        setTotalAssets(currentBalance + stockValue);
      } else {
        setHoldings([]);
        setTotalAssets(currentBalance);
      }
    } catch (error) {
      console.error("Fetch error", error);
    } finally {
      setLoading(false);
    }
  };

  // Reload data when tab changes
  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const cancelOrder = async (orderId) => {
    if (!confirm("Cancel this order?")) return;
    try {
      const res = await fetch(`${API_BASE_URL}/order/cancel-order/${orderId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        fetchData();
      } else {
        alert("Failed to cancel order");
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="flex h-screen bg-gray-50 dark:bg-gray-900 transition-colors duration-300 overflow-hidden">
      {/* Sidebar */}
      <div className="w-20 lg:w-64 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 flex flex-col h-full transition-all duration-300 flex-shrink-0">
        {/* ... sidebar content ... */}
        <div className="h-16 flex items-center justify-center lg:justify-start lg:px-6 border-b border-gray-100 dark:border-gray-700 flex-shrink-0">
          <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-bold text-lg shadow-md shadow-indigo-200 dark:shadow-none">
            P
          </div>
          <span className="hidden lg:block ml-3 font-bold text-gray-800 dark:text-white text-lg">
            Portfolio
          </span>
        </div>

        <nav className="p-4 space-y-2 flex-1 overflow-y-auto">
          {["market", "portfolio", "orders", "trades", "settings"].map(
            (tab) => (
              <button
                key={tab}
                type="button"
                onClick={() => setActiveTab(tab)}
                className={`w-full flex items-center p-3 rounded-xl transition-all capitalize ${
                  activeTab === tab
                    ? "bg-indigo-50 text-indigo-600 dark:bg-indigo-900/20 dark:text-indigo-400"
                    : "text-gray-500 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-gray-700/50"
                }`}
              >
                {tab === "market" && <LayoutDashboard size={20} />}
                {tab === "portfolio" && <PieChart size={20} />}
                {tab === "orders" && <ShoppingCart size={20} />}
                {tab === "trades" && <History size={20} />}
                {tab === "settings" && <Settings size={20} />}
                <span className="hidden lg:block ml-3 font-medium">{tab}</span>
              </button>
            ),
          )}

          {isAdmin && (
            <button
              type="button"
              onClick={() => setActiveTab("admin")}
              className={`w-full flex items-center p-3 rounded-xl transition-all ${
                activeTab === "admin"
                  ? "bg-indigo-50 text-indigo-600 dark:bg-indigo-900/20 dark:text-indigo-400"
                  : "text-gray-500 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-gray-700/50"
              }`}
            >
              <ShieldCheck size={20} />
              <span className="hidden lg:block ml-3 font-medium">Admin</span>
            </button>
          )}
        </nav>

        <div className="p-4 border-t border-gray-100 dark:border-gray-700 space-y-2 flex-shrink-0">
          <button
            type="button"
            onClick={onLogout}
            className="w-full flex items-center p-3 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-xl transition-all"
          >
            <LogOut size={20} />
            <span className="hidden lg:block ml-3 font-medium">Logout</span>
          </button>
        </div>
      </div>

      {/* Floating Theme Toggle */}
      <FloatingThemeToggle isDarkMode={isDarkMode} toggleTheme={toggleTheme} />

      {/* Main Content */}
      <div className="flex-1 flex flex-col h-full overflow-hidden">
        <header className="h-16 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between px-6 lg:px-8 flex-shrink-0">
          <div className="flex items-center bg-gray-100 dark:bg-gray-700 rounded-lg px-3 py-2 w-full max-w-sm">
            <Search size={18} className="text-gray-400" />
            <input
              type="text"
              placeholder="Search stocks..."
              className="bg-transparent border-none outline-none text-sm ml-2 w-full text-gray-900 dark:text-white placeholder-gray-500"
            />
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-3 pl-4 border-l border-gray-200 dark:border-gray-600">
              <div className="text-right hidden md:block">
                <div className="text-sm font-bold text-gray-900 dark:text-white">
                  {user && user.username}
                </div>
                <div className="text-xs text-gray-500 dark:text-gray-400">
                  {user && user.email ? user.email : "User"}
                </div>
              </div>
              <div className="w-9 h-9 bg-gradient-to-tr from-indigo-500 to-purple-500 rounded-lg flex items-center justify-center text-white font-bold">
                {user && user.username
                  ? user.username.charAt(0).toUpperCase()
                  : "U"}
              </div>
            </div>
          </div>
        </header>

        <main className="flex-1 overflow-y-auto p-6 lg:p-8">
          <div className="max-w-7xl mx-auto space-y-8 pb-20">
            {/* Stats Row */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <StatCard
                title="Cash Balance"
                value={`$${Number(balance).toLocaleString()}`}
                icon={Wallet}
                trend={0}
              />
              <StatCard
                title="Active Orders"
                value={activeOrdersCount}
                icon={LineChart}
              />
              <StatCard
                title="Total Assets"
                value={`$${Number(totalAssets).toLocaleString()}`}
                icon={DollarSign}
                trend={0}
              />
            </div>

            {/* --- VIEW LOGIC --- */}

            {activeTab === "market" && (
              <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                <div className="xl:col-span-2 bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden flex flex-col">
                  {/* ... Market Table Header & Content ... */}
                  <div className="p-6 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center">
                    <h2 className="text-lg font-bold text-gray-900 dark:text-white">
                      Market Overview
                    </h2>
                    <button
                      type="button"
                      onClick={fetchData}
                      className="text-indigo-600 dark:text-indigo-400 text-sm font-medium hover:underline flex items-center gap-1"
                    >
                      <RefreshCw size={14} /> Refresh
                    </button>
                  </div>
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="bg-gray-50/50 dark:bg-gray-900/50 text-xs text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          <th className="px-6 py-4 font-semibold">Symbol</th>
                          <th className="px-6 py-4 font-semibold">Sector</th>
                          <th className="px-6 py-4 font-semibold">Price</th>
                          <th className="px-6 py-4 font-semibold text-right">
                            Actions
                          </th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                        {loading ? (
                          <tr>
                            <td
                              colSpan="4"
                              className="text-center py-8 text-gray-500"
                            >
                              Loading market data...
                            </td>
                          </tr>
                        ) : stocks.length === 0 ? (
                          <tr>
                            <td
                              colSpan="4"
                              className="text-center py-8 text-gray-500"
                            >
                              No stocks found.
                            </td>
                          </tr>
                        ) : (
                          stocks.map((stock) => (
                            <tr
                              key={stock.id}
                              className="hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors"
                            >
                              <td className="px-6 py-4">
                                <div className="font-bold text-gray-900 dark:text-white">
                                  {stock.symbol}
                                </div>
                                <div className="text-xs text-gray-500">
                                  {stock.companyName}
                                </div>
                              </td>
                              <td className="px-6 py-4 text-gray-600 dark:text-gray-300 text-sm">
                                {stock.sector || "N/A"}
                              </td>
                              <td className="px-6 py-4 font-mono font-medium text-gray-900 dark:text-white">
                                $
                                {stock.currentPrice
                                  ? stock.currentPrice.toFixed(2)
                                  : "0.00"}
                              </td>
                              <td className="px-6 py-4 text-right space-x-2">
                                <button
                                  type="button"
                                  onClick={() =>
                                    setTradeModal({
                                      isOpen: true,
                                      type: "BUY",
                                      stock,
                                    })
                                  }
                                  className="px-3 py-1.5 rounded-lg bg-green-50 text-green-700 text-xs font-bold hover:bg-green-100 dark:bg-green-900/20 dark:text-green-400 dark:hover:bg-green-900/30 transition-colors"
                                >
                                  Buy
                                </button>
                                <button
                                  type="button"
                                  onClick={() =>
                                    setTradeModal({
                                      isOpen: true,
                                      type: "SELL",
                                      stock,
                                    })
                                  }
                                  className="px-3 py-1.5 rounded-lg bg-red-50 text-red-700 text-xs font-bold hover:bg-red-100 dark:bg-red-900/20 dark:text-red-400 dark:hover:bg-red-900/30 transition-colors"
                                >
                                  Sell
                                </button>
                              </td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>

                <div className="xl:col-span-1">
                  <LiveMarketWidget
                    liveTrades={liveTrades}
                    connectionStatus={connectionStatus}
                  />
                </div>
              </div>
            )}

            {activeTab === "portfolio" && (
              <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden">
                {/* ... Portfolio Table ... */}
                <div className="p-6 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center">
                  <h2 className="text-lg font-bold text-gray-900 dark:text-white">
                    My Portfolio
                  </h2>
                  <button
                    type="button"
                    onClick={fetchData}
                    className="text-indigo-600 dark:text-indigo-400 text-sm font-medium hover:underline flex items-center gap-1"
                  >
                    <RefreshCw size={14} /> Refresh
                  </button>
                </div>
                {holdings.length === 0 ? (
                  <div className="text-center py-20 text-gray-500 dark:text-gray-400">
                    <PieChart size={64} className="mx-auto mb-4 opacity-50" />
                    <h3 className="text-xl font-semibold mb-2">
                      Portfolio Empty
                    </h3>
                    <p>You don't own any stocks yet.</p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="bg-gray-50 dark:bg-gray-700 text-xs uppercase text-gray-500 dark:text-gray-300">
                          <th className="px-6 py-4 font-semibold">Stock</th>
                          <th className="px-6 py-4 font-semibold text-right">
                            Quantity
                          </th>
                          <th className="px-6 py-4 font-semibold text-right">
                            Avg. Price
                          </th>
                          <th className="px-6 py-4 font-semibold text-right">
                            Current Price
                          </th>
                          <th className="px-6 py-4 font-semibold text-right">
                            Total Value
                          </th>
                          <th className="px-6 py-4 font-semibold text-right">
                            Actions
                          </th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                        {holdings.map((item, index) => {
                          const symbol = item.stockSymbol || "Unknown";
                          const currentPrice = item.currentPrice || 0;
                          const quantity = item.quantity || 0;
                          const totalValue =
                            item.totalValue || quantity * currentPrice;
                          const avgPrice = item.avgPrice || 0;
                          const isGain = currentPrice >= avgPrice;

                          return (
                            <tr
                              key={index}
                              className="hover:bg-gray-50 dark:hover:bg-gray-700/20"
                            >
                              <td className="px-6 py-4 font-bold text-gray-900 dark:text-white">
                                {symbol}
                              </td>
                              <td className="px-6 py-4 text-right text-gray-600 dark:text-gray-300">
                                {quantity}
                              </td>
                              <td className="px-6 py-4 text-right text-gray-600 dark:text-gray-300">
                                ${avgPrice.toFixed(2)}
                              </td>
                              <td
                                className={`px-6 py-4 text-right font-medium ${
                                  isGain
                                    ? "text-green-600 dark:text-green-400"
                                    : "text-red-600 dark:text-red-400"
                                }`}
                              >
                                ${currentPrice.toFixed(2)}
                              </td>
                              <td className="px-6 py-4 text-right font-bold text-gray-900 dark:text-white">
                                ${totalValue.toFixed(2)}
                              </td>
                              <td className="px-6 py-4 text-right">
                                <button
                                  onClick={() =>
                                    setTradeModal({
                                      isOpen: true,
                                      type: "SELL",
                                      stockSymbol: symbol,
                                      currentPrice: currentPrice,
                                    })
                                  }
                                  className="px-3 py-1.5 rounded-lg bg-red-50 text-red-700 text-xs font-bold hover:bg-red-100 dark:bg-red-900/20 dark:text-red-400 transition-colors"
                                >
                                  Sell
                                </button>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}

            {activeTab === "orders" && (
              <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
                <div className="xl:col-span-2 space-y-6">
                  {/* ... Place Order Buttons ... */}
                  <div className="bg-white dark:bg-gray-800 p-6 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm">
                    <div className="flex justify-between items-center mb-4">
                      <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
                        <ShoppingCart size={20} /> Place New Order
                      </h3>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {stocks.map((stock) => (
                        <button
                          key={stock.id}
                          onClick={() =>
                            setTradeModal({
                              isOpen: true,
                              type: "BUY",
                              stock: stock,
                              currentPrice: stock.currentPrice,
                            })
                          }
                          className="p-4 rounded-xl border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700/50 text-left transition-all group"
                        >
                          <div className="flex justify-between items-start">
                            <div>
                              <div className="font-bold text-gray-900 dark:text-white">
                                {stock.symbol}
                              </div>
                              <div className="text-xs text-gray-500">
                                {stock.companyName}
                              </div>
                            </div>
                            <div className="text-sm font-medium text-indigo-600 dark:text-indigo-400">
                              ${stock.currentPrice?.toFixed(2)}
                            </div>
                          </div>
                          <div className="mt-2 text-xs text-gray-400 group-hover:text-indigo-500 transition-colors">
                            Click to Buy/Sell
                          </div>
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* ... Order History ... */}
                  <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden">
                    <div className="p-6 border-b border-gray-100 dark:border-gray-700">
                      <h3 className="text-lg font-bold text-gray-900 dark:text-white">
                        Order History
                      </h3>
                    </div>
                    {userOrders.length === 0 ? (
                      <div className="p-8 text-center text-gray-500">
                        No orders found.
                      </div>
                    ) : (
                      <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                          <thead>
                            <tr className="bg-gray-50 dark:bg-gray-700 text-xs uppercase text-gray-500 dark:text-gray-300">
                              <th className="px-6 py-4">Time</th>
                              <th className="px-6 py-4">Type</th>
                              <th className="px-6 py-4">Stock</th>
                              <th className="px-6 py-4 text-right">Quantity</th>
                              <th className="px-6 py-4 text-right">Price</th>
                              <th className="px-6 py-4 text-center">Status</th>
                              <th className="px-6 py-4 text-right">Action</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                            {userOrders.map((order) => (
                              <tr
                                key={order.id}
                                className="hover:bg-gray-50 dark:hover:bg-gray-700/20"
                              >
                                <td className="px-6 py-4 text-sm text-gray-500">
                                  {new Date(order.timestamp).toLocaleString()}
                                </td>
                                <td className="px-6 py-4">
                                  <span
                                    className={`px-2 py-1 rounded text-xs font-bold ${
                                      order.type === "BUY"
                                        ? "bg-green-100 text-green-800"
                                        : "bg-red-100 text-red-800"
                                    }`}
                                  >
                                    {order.type}
                                  </span>
                                </td>
                                <td className="px-6 py-4 font-bold text-gray-900 dark:text-white">
                                  {order.stock?.symbol}
                                </td>
                                <td className="px-6 py-4 text-right text-gray-600 dark:text-gray-300">
                                  {order.quantity}
                                </td>
                                <td className="px-6 py-4 text-right text-gray-600 dark:text-gray-300">
                                  ${order.price?.toFixed(2)}
                                </td>
                                <td className="px-6 py-4 text-center">
                                  <span
                                    className={`px-2 py-1 rounded text-xs ${
                                      order.status === "FILLED"
                                        ? "bg-blue-100 text-blue-800"
                                        : order.status === "CANCELLED"
                                          ? "bg-gray-100 text-gray-800"
                                          : "bg-yellow-100 text-yellow-800"
                                    }`}
                                  >
                                    {order.status}
                                  </span>
                                </td>
                                <td className="px-6 py-4 text-right">
                                  {(order.status === "PENDING" ||
                                    order.status === "PARTIALLY_FILLED") && (
                                    <button
                                      onClick={() => cancelOrder(order.id)}
                                      className="text-red-500 hover:text-red-700 text-xs font-bold uppercase"
                                    >
                                      Cancel
                                    </button>
                                  )}
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </div>
                </div>
                <div className="xl:col-span-1">
                  <LiveMarketWidget
                    liveTrades={liveTrades}
                    connectionStatus={connectionStatus}
                  />
                </div>
              </div>
            )}

            {activeTab === "trades" && (
              <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                {/* History Component */}
                <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden h-full min-h-[600px] flex flex-col">
                  <div className="p-6 border-b border-gray-100 dark:border-gray-700">
                    <h3 className="text-lg font-bold text-gray-900 dark:text-white flex items-center gap-2">
                      <History size={20} /> My Trade History
                    </h3>
                  </div>
                  <div className="flex-1 overflow-hidden flex flex-col">
                    <TradesList user={user} token={token} />
                  </div>
                </div>

                {/* Live Feed Component */}
                <LiveMarketWidget
                  liveTrades={liveTrades}
                  connectionStatus={connectionStatus}
                />
              </div>
            )}

            {activeTab === "settings" && (
              <SettingsPanel
                user={fullUser}
                token={token}
                onUpdate={fetchData}
              />
            )}

            {activeTab === "admin" && isAdmin && <AdminPanel token={token} />}
          </div>
        </main>
      </div>

      {(tradeModal.stock || tradeModal.stockSymbol) && (
        <TradeModal
          isOpen={tradeModal.isOpen}
          onClose={() => setTradeModal({ ...tradeModal, isOpen: false })}
          type={tradeModal.type}
          stockSymbol={tradeModal.stock?.symbol || tradeModal.stockSymbol}
          currentPrice={tradeModal.currentPrice}
          token={token}
          username={user && user.username}
          onTradeComplete={fetchData}
        />
      )}
    </div>
  );
};

window.Dashboard = Dashboard;
