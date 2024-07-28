import { Link, useRouteError } from "react-router-dom";
import { AppHeader, Footer } from "./main-layout.tsx";

export default function ErrorBoundary() {
  const error = useRouteError() as Error | null | undefined;

  return (
    <div className="flex grow flex-col p-2">
      <AppHeader />
      <div className="flex w-full grow flex-col items-center justify-center gap-4">
        <h1 className="text-xl">Ooops! Something went wrong</h1>
        {error?.message && (
          <pre className="max-w-80 whitespace-pre-wrap rounded-md bg-neutral p-2 text-center font-mono text-sm">{error.message}</pre>
        )}
        <Link className="btn btn-primary" to="/">
          Home
        </Link>
      </div>
      <Footer />
    </div>
  );
}
