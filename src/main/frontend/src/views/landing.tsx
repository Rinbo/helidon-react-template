import { Link } from "react-router-dom";
import RegistrationForm from "./registration/registration-form.tsx";
import LogoutForm from "./logout/logout-form.tsx";
import { useAuth } from "./main-layout.tsx";

export default function Landing() {
  const { principal } = useAuth();

  return (
    <section className="flex h-full flex-col items-center justify-center gap-4">
      <h1 className="font-mono text-4xl font-bold md:text-5xl">borjessons.dev</h1>
      {!principal && <RegistrationForm />}
      <Link className="link-info" to="/about">
        About
      </Link>
      {principal ? <LogoutForm /> : <Link to={"/login"}>Login</Link>}
    </section>
  );
}
