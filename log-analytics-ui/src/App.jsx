import { useState } from "react";
import "./App.css";

function App() {
  const [year, setYear] = useState("2026");
  const [month, setMonth] = useState("09");
  const [day, setDay] = useState("13");
  const [orderId, setOrderId] = useState("ORD-TCP-003");

  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function searchLogs(event) {
    event.preventDefault();

    setLoading(true);
    setError("");
    setLogs([]);

    const params = new URLSearchParams({
      year,
      month,
      day,
      orderId,
    });

    try {
      const response = await fetch(
        `http://localhost:8080/api/athena/logs?${params}`
      );

      if (!response.ok) {
        throw new Error(`Request failed with status ${response.status}`);
      }

      const data = await response.json();
      setLogs(data);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="page">
      <section className="container">
        <header>
          <p className="eyebrow">AWS Athena + Spring Boot</p>
          <h1>Application Log Analytics</h1>
          <p className="subtitle">
            Search archived application logs using an order ID and date.
          </p>
        </header>

        <form className="search-form" onSubmit={searchLogs}>
          <label>
            Year
            <input
              value={year}
              onChange={(event) => setYear(event.target.value)}
              required
            />
          </label>

          <label>
            Month
            <input
              value={month}
              onChange={(event) => setMonth(event.target.value)}
              required
            />
          </label>

          <label>
            Day
            <input
              value={day}
              onChange={(event) => setDay(event.target.value)}
              required
            />
          </label>

          <label className="order-field">
            Order ID
            <input
              value={orderId}
              onChange={(event) => setOrderId(event.target.value)}
              required
            />
          </label>

          <button type="submit" disabled={loading}>
            {loading ? "Searching Athena..." : "Search logs"}
          </button>
        </form>

        {error && <p className="error">{error}</p>}

        {!loading && !error && logs.length === 0 && (
          <p className="empty">No logs loaded.</p>
        )}

        {logs.length > 0 && (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Level</th>
                  <th>Order ID</th>
                  <th>Message</th>
                  <th>Correlation ID</th>
                </tr>
              </thead>

              <tbody>
                {logs.map((log, index) => (
                  <tr key={`${log.correlation_id}-${index}`}>
                    <td>{log.event_timestamp}</td>
                    <td>
                      <span className={`level ${log.level?.toLowerCase()}`}>
                        {log.level}
                      </span>
                    </td>
                    <td>{log.order_id}</td>
                    <td>{log.message}</td>
                    <td>{log.correlation_id}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}

export default App;