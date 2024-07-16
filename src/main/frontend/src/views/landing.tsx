import { Link, useOutletContext } from "react-router-dom";
import RegistrationForm from "./registration/registration-form.tsx";
import { Principal } from "../auth/auth.ts";

export default function Landing() {
  const principal = useOutletContext<Principal>();

  return (
    <section className="flex h-full flex-col items-center justify-center gap-4">
      <h1 className="font-mono text-4xl font-bold md:text-5xl">borjessons.dev</h1>

      {!principal && <RegistrationForm />}
      <Link className="btn btn-primary" to="/about">
        About
      </Link>
      <Link to={"/login"}>Login</Link>
    </section>
  );
}
