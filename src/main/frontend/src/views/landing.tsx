import { Link } from "react-router-dom";
import LogoutForm from "./logout/logout-form.tsx";
import { useAuth } from "./main-layout.tsx";

export default function Landing() {
  const { principal } = useAuth();

  return (
    <section className="flex h-full items-center justify-center">
      <div className="hero mx-auto max-w-xl rounded-xl">
        <div className="hero-content text-center">
          <div>
            <h1 className="my-10 font-mono text-4xl font-bold md:text-5xl">borjessons.dev</h1>
            <p className="py-6">Hello there and welcome</p>
            <Link className="link-info" to="/about">
              About
            </Link>
            {principal && <LogoutForm />}
            {!principal && (
              <Link className="link-info block" to={"/login"}>
                Login
              </Link>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
