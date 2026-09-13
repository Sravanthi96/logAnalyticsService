import { useState } from "react";
import "./App.css";

function App() {
  const [year, setYear] = useState("");
  const [month, setMonth] = useState("");
  const [day, setDay] = useState("");
  const [orderId, setOrderId] = useState("");

  const currentYear = new Date().getFullYear();

  const years = Array.from(
    { length: 5 },
    (_, index) => String(currentYear - index)
  );

  const months = [
    { value: "01", label: "January" },
    { value: "02", label: "February" },
    { value: "03", label: "March" },
    { value: "04", label: "April" },
    { value: "05", label: "May" },
    { value: "06", label: "June" },
    { value: "07", label: "July" },
    { value: "08", label: "August" },
    { value: "09", label: "September" },
    { value: "10", label: "October" },
    { value: "11", label: "November" },
    { value: "12", label: "December" },
  ];

  const numberOfDays =
    year && month
      ? new Date(Number(year), Number(month), 0).getDate()
      : 31;

  const days = Array.from(
    { length: numberOfDays },
    (_, index) => String(index + 1).padStart(2, "0")
  );

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
          <h1>Log Analytics</h1>
          <p className="subtitle">
            Search archived application logs using an order ID and date.
          </p>
        </header>

        <form className="search-form" onSubmit={searchLogs}>
          <label>
            Year
            <select
              value={year}
              onChange={(event) => {
                setYear(event.target.value);
                setDay("");
              }}
              required
            >
              <option value="">Select year</option>

              {years.map((yearOption) => (
                <option key={yearOption} value={yearOption}>
                  {yearOption}
                </option>
              ))}
            </select>
          </label>

          <label>
            Month
            <select
              value={month}
              onChange={(event) => {
                setMonth(event.target.value);
                setDay("");
              }}
              required
            >
              <option value="">Select month</option>

              {months.map((monthOption) => (
                <option key={monthOption.value} value={monthOption.value}>
                  {monthOption.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            Day
            <select
              value={day}
              onChange={(event) => setDay(event.target.value)}
              required
              disabled={!year || !month}
            >
              <option value="">Select day</option>

              {days.map((dayOption) => (
                <option key={dayOption} value={dayOption}>
                  {dayOption}
                </option>
              ))}
            </select>
          </label>

          <label className="order-field">
            Order ID
            <input
              value={orderId}
              onChange={(event) => setOrderId(event.target.value)}
              placeholder="Example: ORD-TCP-003"
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