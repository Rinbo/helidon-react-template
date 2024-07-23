import { Link } from "react-router-dom";
import LogoutForm from "./logout/logout-form.tsx";
import { useAuth } from "./main-layout.tsx";
import { HiTemplate } from "react-icons/hi";
import React from "react";

export default function Landing() {
  const { principal } = useAuth();

  return (
    <section className="flex h-full items-center justify-center">
      <div className="hero max-w-lg rounded-3xl py-8 text-center sm:px-8">
        <div className="hero-content flex-col">
          <HiTemplate className="text-9xl text-primary" />
          <div>
            <h1 className="my-10 font-mono text-4xl font-bold md:text-5xl">borjessons.dev</h1>
            <p className="py-6">
              Revolutionize your workflow with <span className="font-mono text-primary">borjessons.dev</span>. Our cutting-edge platform
              integrates seamlessly with your existing tools, boosting productivity and streamlining collaboration across your entire
              organization.
            </p>
            <div className="flex flex-row justify-center gap-4">
              <Link className="btn btn-primary" to="/users">
                Users
              </Link>
              {principal && <LogoutForm className="btn btn-secondary" />}
              {!principal && (
                <React.Fragment>
                  <Link className="btn btn-info" to={"/login"}>
                    Login
                  </Link>
                  <Link className="btn btn-secondary" to={"/register"}>
                    Register
                  </Link>
                </React.Fragment>
              )}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
