import { json, Link, LoaderFunctionArgs, redirect, useLoaderData } from "react-router-dom";
import { authProvider } from "../../auth/auth.ts";

// TODO Remove when keycode implementation is stable -  Currently not in use
export async function loader({ request }: LoaderFunctionArgs) {
  if (await authProvider.isAuthenticated()) return redirect("/");

  const searchParams = new URLSearchParams(new URL(request.url).search);
  const email = searchParams.get("email");
  const passcode = searchParams.get("passcode");

  if (!passcode || !email) return json({ error: "Missing login credentials. Please try logging in again." });

  try {
    await authProvider.authenticate({ email, passcode });
  } catch (error) {
    console.error(error, "Authentication failed");
    return json({ error: "Authentication failed" });
  }

  return json({ success: true });
}

export default function AuthenticationView() {
  const { success, error } = useLoaderData() as { success?: boolean; error?: string };

  if (!success)
    return (
      <div className="flex h-full flex-col items-center justify-center gap-2">
        <div>Authentication failed with message</div>
        <pre className="text-error">{error}</pre>
        <Link to="/" className="link">
          Go Home
        </Link>
      </div>
    );

  return (
    <div className="flex h-full flex-col items-center justify-center gap-2">
      <h1 className={"text-3xl text-accent"}>Authentication Successful</h1>
      <div>You may continue from here or close this window and return to where you logged in</div>
      <Link to="/">Home</Link>
      <button className="btn btn-info" onClick={() => close()}>
        Close Tab
      </button>
    </div>
  );
}
