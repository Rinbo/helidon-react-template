import { HiTemplate } from "react-icons/hi";
import { Link } from "react-router-dom";

export default function AppLogo() {
  return (
    <Link to="/" className="btn btn-ghost flex flex-row items-center gap-2">
      <HiTemplate className="text-4xl text-primary sm:text-5xl" />
      <h1 className="text-md font-mono sm:text-lg">borjessons.dev</h1>
    </Link>
  );
}
