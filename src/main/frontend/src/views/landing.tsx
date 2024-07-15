import logo from "../assets/react.svg";
import { Link } from "react-router-dom";
import RegistrationForm from "./registration/registration-form.tsx";

export default function Landing() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-4 p-4">
      <h1 className="font-mono text-4xl font-bold md:text-5xl">borjessons.dev</h1>
      <img className="grow p-4" src={logo} alt="logo" />
      <RegistrationForm />
      <Link className="btn btn-primary" to="/about">
        About
      </Link>
      <Link to={"/login"}>Login</Link>
    </div>
  );
}
