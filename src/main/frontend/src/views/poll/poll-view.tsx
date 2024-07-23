import React from "react";
import { authProvider } from "../../auth/auth.ts";
import { Link, useNavigate, useRevalidator } from "react-router-dom";

// TODO Remove when keycode implementation is stable -  Currently not in use
export default function PollView() {
  const count = React.useRef(0);
  const [error, setError] = React.useState<boolean>(false);
  const navigate = useNavigate();
  const revalidator = useRevalidator();

  React.useEffect(() => {
    const interval = setInterval(async () => {
      count.current += 1;
      await authProvider.fetchPrincipal();

      if (authProvider.principal) {
        revalidator.revalidate();
        navigate("/", { replace: true });
      }

      if (count.current > 80) {
        clearInterval(interval);
        setError(true);
      }
    }, 2000);
    return () => clearInterval(interval);
  }, []);

  if (error)
    return (
      <div className="flex h-full flex-col items-center justify-center gap-4">
        <h1 className="text-3xl text-warning">Your login link has timed out</h1>
        <p>Please go back to apply for another one</p>
        <Link to="/login" className={"link"}>
          Login
        </Link>
      </div>
    );

  return (
    <div className="flex h-full flex-col items-center justify-center gap-4">
      <h1 className="text-3xl text-accent">Please check your email</h1>
      <p className="animate-bounce">Click the link that we sent you to log in</p>
      <progress className="progress progress-accent mt-2 max-w-md"></progress>
    </div>
  );
}
