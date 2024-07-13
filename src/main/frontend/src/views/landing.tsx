import logo from "../assets/react.svg";
import { Link } from "react-router-dom";

export default function Landing() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-4 p-4">
      <h1 className="text-3xl font-bold">Hello world!</h1>
      <img src={logo} alt="logo" width={600} />
      <Link className="btn btn-primary" to="/about">
        About
      </Link>
      <Link to={"/login"}>Login</Link>
    </div>
  );
}
