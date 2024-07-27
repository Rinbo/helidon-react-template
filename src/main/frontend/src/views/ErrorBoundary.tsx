import { Link } from "react-router-dom";
import { AppHeader, Footer } from "./main-layout.tsx";

export default function ErrorBoundary() {
  return (
    <div className="flex grow flex-col p-2">
      <AppHeader />
      <div className="flex w-full grow flex-col items-center justify-center gap-4">
        <h1 className="text-xl">Ooops! Something went wrong</h1>
        <Link className="btn btn-primary" to="/">
          Home
        </Link>
      </div>
      <Footer />
    </div>
  );
}
