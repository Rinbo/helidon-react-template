import { HiTemplate } from "react-icons/hi";
import { Link } from "react-router-dom";

export default function AppLogo() {
  return (
    <Link to="/" className="btn btn-ghost flex flex-row items-center gap-2">
      <HiTemplate size={45} className="text-secondary" />
      <h1 className="font-mono text-lg">borjessons.dev</h1>
    </Link>
  );
}
