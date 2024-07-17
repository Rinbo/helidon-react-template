import AppLogo from "./app-logo.tsx";

export default function AppHeader() {
  return (
    <header className="navbar bg-base-100">
      <div className="navbar-start">
        <AppLogo />
      </div>
    </header>
  );
}
