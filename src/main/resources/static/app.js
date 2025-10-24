document.addEventListener("DOMContentLoaded", () => {
  // --- Configuration ---
  const BASE_URL = "http://localhost:8080";

  // --- State ---
  let stompClient = null;
  let jwtToken = localStorage.getItem("jwtToken");
  let currentUser = localStorage.getItem("currentUser");
  let priceCache = {}; // To store last price for change calculation
  let appToast; // Bootstrap Toast instance

  // --- DOM Elements ---
  const authContainer = document.getElementById("auth-container");
  const appContainer = document.getElementById("app-container");
  const loginCard = document.getElementById("login-card");
  const signupCard = document.getElementById("signup-card");

  const loginForm = document.getElementById("login-form");
  const signupForm = document.getElementById("signup-form");
  const orderForm = document.getElementById("order-form");
  const addBalanceForm = document.getElementById("add-balance-form");

  const showSignupLink = document.getElementById("show-signup");
  const showLoginLink = document.getElementById("show-login");
  const logoutButton = document.getElementById("logout-button");

  const pricesTableBody = document.getElementById("prices-table-body");
  const stocksTableBody = document.getElementById("stocks-table-body");

  const userInfo = document.getElementById("user-info");
  const userBalanceInfo = document.getElementById("user-balance");
  const toastElement = document.getElementById("app-toast");
  const toastBody = document.getElementById("toast-body");

  // --- Initialization ---
  if (toastElement) {
    appToast = new bootstrap.Toast(toastElement);
  }

  if (jwtToken && currentUser) {
    showAppContainer();
  } else {
    showAuthContainer();
  }

  // --- Event Listeners ---
  if (showSignupLink) {
    showSignupLink.addEventListener("click", (e) => {
      e.preventDefault();
      loginCard.classList.add("d-none");
      signupCard.classList.remove("d-none");
    });
  }

  if (showLoginLink) {
    showLoginLink.addEventListener("click", (e) => {
      e.preventDefault();
      signupCard.classList.add("d-none");
      loginCard.classList.remove("d-none");
    });
  }

  if (loginForm) loginForm.addEventListener("submit", handleLogin);
  if (signupForm) signupForm.addEventListener("submit", handleSignup);
  if (logoutButton) logoutButton.addEventListener("click", handleLogout);
  if (orderForm) orderForm.addEventListener("submit", handleOrder);
  if (addBalanceForm)
    addBalanceForm.addEventListener("submit", handleAddBalance);

  // --- UI Functions ---
  function showAppContainer() {
    if (authContainer) authContainer.classList.add("d-none");
    if (appContainer) appContainer.classList.remove("d-none");

    if (userInfo) userInfo.textContent = `Welcome, ${currentUser}`;

    // Fetch initial data
    loadUserBalance();
    loadAllCompanies();

    // Connect to WebSocket
    connectWebSocket();
  }

  function showAuthContainer() {
    if (appContainer) appContainer.classList.add("d-none");
    if (authContainer) authContainer.classList.remove("d-none");

    if (userBalanceInfo) userBalanceInfo.textContent = ""; // Clear balance on logout

    if (stompClient && stompClient.connected) {
      // Use .connected for v2.3.3
      stompClient.disconnect();
    }
  }

  function showToast(message, isError = false) {
    if (!appToast) return;

    toastBody.textContent = message;

    // Remove old classes and add new one
    toastElement.classList.remove("text-bg-success", "text-bg-danger");
    if (isError) {
      toastElement.classList.add("text-bg-danger");
    } else {
      toastElement.classList.add("text-bg-success");
    }

    appToast.show();
  }

  // --- Authentication ---
  async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById("login-username").value;
    const password = document.getElementById("login-password").value;

    try {
      const response = await fetch(`${BASE_URL}/public/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Login failed");
      }

      // Get token from response body's 'message' field
      const data = await response.json();
      const token = data.message;

      if (!token) {
        throw new Error(
          "Login successful, but no token received in response body.",
        );
      }

      jwtToken = token;
      currentUser = username;

      localStorage.setItem("jwtToken", jwtToken);
      localStorage.setItem("currentUser", currentUser);

      loginForm.reset();
      showAppContainer();
    } catch (error) {
      console.error("Login Error:", error);
      showToast(error.message, true);
    }
  }

  async function handleSignup(e) {
    e.preventDefault();
    const username = document.getElementById("signup-username").value;
    const email = document.getElementById("signup-email").value;
    const password = document.getElementById("signup-password").value;
    const country = document.getElementById("signup-country")
      ? document.getElementById("signup-country").value
      : null; // Handle missing country

    try {
      const response = await fetch(`${BASE_URL}/public/signup`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        // Match User object: userName, password, email, country
        body: JSON.stringify({ userName: username, email, password, country }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Signup failed");
      }

      showToast(
        "Signup successful! Please check your email to verify your account.",
        false,
      );
      signupForm.reset();

      // Switch back to login
      signupCard.classList.add("d-none");
      loginCard.classList.remove("d-none");
    } catch (error) {
      console.error("Signup Error:", error);
      showToast(error.message, true);
    }
  }

  async function handleLogout() {
    try {
      // Call the backend to invalidate the token
      await fetchWithAuth("/user-panel/logout", { method: "GET" });
      showToast("Logged out successfully.", false);
    } catch (error) {
      // Even if backend fails, log out the client
      console.error("Logout error:", error);
      // Don't show toast if it was an auth error, fetchWithAuth already did
      if (error.message !== "Unauthorized") {
        showToast("Logout failed on server, logging out locally.", true);
      }
    } finally {
      // Clear local storage and state
      jwtToken = null;
      currentUser = null;
      localStorage.removeItem("jwtToken");
      localStorage.removeItem("currentUser");

      // Show auth screen
      showAuthContainer();
    }
  }

  /**
   * Helper function for authenticated fetch calls
   */
  async function fetchWithAuth(url, options = {}) {
    if (!jwtToken) {
      showAuthContainer();
      throw new Error("Not authenticated");
    }

    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${jwtToken}`, // Added "Bearer " prefix
      ...options.headers,
    };

    const response = await fetch(`${BASE_URL}${url}`, { ...options, headers });

    if (response.status === 401 || response.status === 403) {
      // Token is invalid or expired
      showToast("Session expired. Please log in again.", true);
      handleLogout(); // Log out the user
      throw new Error("Unauthorized");
    }

    // Allow 201 Created status as "ok"
    if (!response.ok && response.status !== 201) {
      const errorData = await response.json();
      throw new Error(errorData.message || "An error occurred");
    }

    // Handle responses that might have a body
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
      return response.json();
    } else {
      return; // No JSON body
    }
  }

  /**
   * Handles adding balance from the form
   */
  async function handleAddBalance(e) {
    if (!e) return; // Guard against non-event calls
    e.preventDefault();
    const amountEl = document.getElementById("balance-amount");
    if (!amountEl) return; // Guard if element doesn't exist

    const amount = parseFloat(amountEl.value);

    if (isNaN(amount) || amount <= 0) {
      showToast("Please enter a valid amount.", true);
      return;
    }

    try {
      // Endpoint from spec: /user-panel/add-balance (GET)
      await fetchWithAuth(`/user-panel/add-balance?balance=${amount}`, {
        method: "GET",
      });

      showToast(`$${amount.toFixed(2)} added to your balance.`, false);
      if (addBalanceForm) addBalanceForm.reset();
      loadUserBalance(); // Refresh balance
    } catch (error) {
      if (error.message !== "Unauthorized") {
        console.error("Add Balance Error:", error);
        showToast(error.message, true);
      }
    }
  }

  /**
   * New function to load and display the user's balance
   */
  async function loadUserBalance() {
    if (!userBalanceInfo) return; // Make sure element exists

    userBalanceInfo.textContent = "Loading balance...";
    userBalanceInfo.classList.remove("text-danger");
    userBalanceInfo.classList.add("text-success");

    try {
      // Calling the new endpoint provided by the user: /get-balance
      const data = await fetchWithAuth("/user-panel/get-balance", {
        method: "GET",
      });

      // data format is { message: null, object: 1234.56 } or { object: "1234.56" }
      const balance = data.object; // This could be null, "123.45", or 123.45

      // Treat null, undefined, or unparseable as 0.00
      const balanceValue = parseFloat(balance);

      if (!isNaN(balanceValue)) {
        // Successfully parsed a number
        userBalanceInfo.textContent = `Balance: $${balanceValue.toFixed(2)}`;
      } else {
        // This handles null, undefined, or invalid string
        userBalanceInfo.textContent = `Balance: $0.00`;
      }
    } catch (error) {
      if (error.message !== "Unauthorized") {
        console.error("Error fetching balance:", error);
        userBalanceInfo.textContent = "Error loading balance";
        userBalanceInfo.classList.remove("text-success");
        userBalanceInfo.classList.add("text-danger");
      }
    }
  }

  /**
   * Loads all companies and populates the stock list
   */
  async function loadAllCompanies() {
    if (!stocksTableBody) return;
    try {
      // From OpenAPI spec: /company/all
      // This endpoint doesn't require auth according to the spec.
      const response = await fetch(`${BASE_URL}/company/all`);
      if (!response.ok) throw new Error("Failed to fetch companies");

      const data = await response.json();

      // Data structure from spec: ApiResponseListCompanyDTO
      const companies = data.object;

      stocksTableBody.innerHTML = ""; // Clear existing
      if (!companies || companies.length === 0) {
        stocksTableBody.innerHTML =
          '<tr><td colspan="4" class="text-center">No companies found.</td></tr>';
        return;
      }

      companies.forEach((company) => {
        const row = document.createElement("tr");
        row.innerHTML = `
                     <td>${company.name}</td>
                     <td>${company.tickerSymbol}</td>
                     <td>
                         <button class="btn btn-sm btn-outline-primary rounded-3" onclick="window.selectStock('${company.tickerSymbol}')">Trade</button>
                     </td>
                 `;
        // Note: Removed 'Sector' as it wasn't in the innerHTML
        stocksTableBody.appendChild(row);
      });
    } catch (error) {
      console.error("Error loading companies:", error);
      showToast(error.message, true);
    }
  }

  // Make trade button click accessible globally
  window.selectStock = (symbol) => {
    const orderSymbolEl = document.getElementById("order-symbol");
    if (orderSymbolEl) {
      orderSymbolEl.value = symbol;
    }
  };

  /**
   * Handles placing a BUY or SELL order
   */
  async function handleOrder(e) {
    if (!e) return; // Guard
    e.preventDefault();

    if (!e.submitter) {
      showToast("Could not determine order type.", true);
      return;
    }

    const orderType = e.submitter.id === "buy-button" ? "BUY" : "SELL";
    const endpoint =
      orderType === "BUY" ? "/order/buy-order" : "/order/sell-order";

    const stockSymbolEl = document.getElementById("order-symbol");
    const quantityEl = document.getElementById("order-quantity");
    const priceEl = document.getElementById("order-price");

    if (!stockSymbolEl || !quantityEl || !priceEl) {
      showToast("Order form elements are missing.", true);
      return;
    }

    const stockSymbol = stockSymbolEl.value;
    const quantity = parseInt(quantityEl.value);
    let price = parseFloat(priceEl.value);

    if (isNaN(price) || price <= 0) {
      showToast("Please enter a valid limit price.", true);
      return;
    }
    if (isNaN(quantity) || quantity <= 0) {
      showToast("Please enter a valid quantity.", true);
      return;
    }
    if (!stockSymbol) {
      showToast("Please select a stock symbol.", true);
      return;
    }

    // Find the stock ID from the symbol
    let stockId;
    try {
      // /stock/search is a public endpoint
      const stockSearch = await fetch(
        `${BASE_URL}/stock/search?name=${stockSymbol}`,
      );
      if (!stockSearch.ok) throw new Error("Stock search request failed");

      const stockData = await stockSearch.json();

      // Check if stock was found and has an ID
      if (!stockData.object || stockData.object.length === 0) {
        throw new Error("Stock symbol not found");
      }

      // Assuming the DTO now includes an 'id'. Let's find the exact match.
      const foundStock = stockData.object.find(
        (s) => s.symbol.toUpperCase() === stockSymbol.toUpperCase(),
      );

      if (!foundStock) {
        throw new Error(`No exact match found for symbol ${stockSymbol}`);
      }

      // This is the crucial change: assuming 'id' is now in the StockDTO
      stockId = foundStock.id;

      if (!stockId) {
        throw new Error(
          "Stock was found, but its ID is missing. Cannot place order.",
        );
      }

      // Now we can create the payload
      const orderPayload = {
        // The backend should get the user from the JWT token
        stock: { id: stockId }, // We now have the ID!
        quantity,
        price,
        type: orderType,
        // Backend sets status automatically
      };

      // Use fetchWithAuth to send the order
      const result = await fetchWithAuth(endpoint, {
        method: "POST",
        body: JSON.stringify(orderPayload),
      });

      showToast(`${orderType} order placed successfully!`, false);
      if (orderForm) orderForm.reset();
      loadUserBalance(); // Refresh balance after order
      // A real-time update should come via the /user/queue/orders websocket
    } catch (error) {
      if (error.message !== "Unauthorized") {
        console.error("Order Error:", error);
        showToast(error.message, true);
      }
    }
  }

  // --- WebSocket (REWRITTEN FOR STOMP v2.3.3) ---
  function connectWebSocket() {
    if (!jwtToken) {
      console.error("Cannot connect to WebSocket without JWT token.");
      return;
    }

    if (stompClient && stompClient.connected) {
      console.log("STOMP client already connected.");
      return;
    }

    try {
      // 1. Create the SockJS connection
      // We're now using the Stomp library, which should be globally available
      const socket = new SockJS(`${BASE_URL}/ws`);

      // 2. Create STOMP client over the socket
      stompClient = Stomp.over(socket);

      // 3. Set debug (optional)
      stompClient.debug = (str) => {
        // console.log('STOMP: ' + str); // Uncomment for verbose logging
      };

      // 4. Define the connect callback
      const onConnect = (frame) => {
        console.log("Connected: " + frame);
        showToast("Connected to live updates!", false);

        // Subscribe to public prices
        stompClient.subscribe("/topic/prices", onPricesReceived);

        // Subscribe to user-specific topics
        stompClient.subscribe(`/user/queue/orders`, onUserOrderUpdate);
        stompClient.subscribe(`/user/queue/errors`, onErrorReceived);
      };

      // 5. Define the error callback
      const onError = (frame) => {
        // frame is usually a string message in v2.3.3, but can be an object
        console.error("STOMP error:", frame);
        let errorMessage = "STOMP Connection Error";

        if (typeof frame === "object" && frame.body) {
          errorMessage = frame.body;
        } else if (typeof frame === "string") {
          errorMessage = frame;
        }

        if (errorMessage.includes("token") || errorMessage.includes("403")) {
          showToast("Live update connection failed (Auth). Logging out.", true);
          handleLogout();
        } else {
          showToast("Connection error: " + errorMessage, true);
        }
      };

      // 6. Connect
      const headers = {
        Authorization: jwtToken, // STOMP v2.3.3 takes headers here
      };
      stompClient.connect(headers, onConnect, onError);
    } catch (error) {
      console.error("STOMP connection failed to initialize:", error);
      // This catch block will run if 'Stomp' or 'SockJS' is not defined
      showToast("Live update client failed: " + error.message, true);
    }
  }

  function onPricesReceived(payload) {
    if (!pricesTableBody) return;

    // Assuming payload.body is a JSON string of a single StockDTO or list
    let prices = [];
    try {
      const priceData = JSON.parse(payload.body);
      prices = Array.isArray(priceData) ? priceData : [priceData];
    } catch (e) {
      console.error("Failed to parse price data:", payload.body);
      return;
    }

    if (pricesTableBody.querySelector(".text-muted")) {
      pricesTableBody.innerHTML = ""; // Clear "waiting" message
    }

    prices.forEach((stock) => {
      // Assuming the DTO now has an 'id' and 'symbol'
      const symbol = stock.symbol;
      const newPrice = stock.currentPrice;
      const openPrice = stock.openPrice;

      if (!symbol) return; // Skip if no symbol

      let change = 0;
      let changeClass = "price-no-change";

      if (priceCache[symbol]) {
        change = newPrice - priceCache[symbol];
      } else if (openPrice) {
        change = newPrice - openPrice;
      }

      if (change > 0) changeClass = "price-up";
      if (change < 0) changeClass = "price-down";

      priceCache[symbol] = newPrice; // Update cache

      let row = document.getElementById(`price-row-${symbol}`);
      if (!row) {
        row = document.createElement("tr");
        row.id = `price-row-${symbol}`;
        row.innerHTML = `
                    <td class="fw-bold">${symbol}</td>
                    <td class="price-cell"></td>
                    <td class="change-cell"></td>
                `;
        pricesTableBody.appendChild(row);
      }

      const priceCell = row.querySelector(".price-cell");
      const changeCell = row.querySelector(".change-cell");

      priceCell.textContent = `$${newPrice.toFixed(2)}`;
      changeCell.textContent = `${change > 0 ? "+" : ""}${change.toFixed(2)}`;

      changeCell.className = `change-cell ${changeClass}`;

      // Add flash effect
      row.classList.add("price-flash");
      setTimeout(() => row.classList.remove("price-flash"), 1000);
    });
  }

  function onUserOrderUpdate(payload) {
    console.log("User Order Update:", payload.body);
    try {
      const order = JSON.parse(payload.body);
      showToast(
        `Order Update: ${order.stock.symbol} status is now ${order.status}`,
        false,
      );
      // Here you would update a "My Orders" table
    } catch (e) {
      console.error("Failed to parse order update:", payload.body);
    }
  }

  function onErrorReceived(payload) {
    console.error("User Error:", payload.body);
    try {
      const error = JSON.parse(payload.body);
      showToast(`Error: ${error.message}`, true);
    } catch (e) {
      showToast(`Error: ${payload.body}`, true);
    }
  }
});
