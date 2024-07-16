import { useFetcher } from "react-router-dom";

export default function LogoutForm() {
  const fetcher = useFetcher();

  return (
    <fetcher.Form action="/logout" method="post">
      <button className="btn btn-ghost">Logout</button>
    </fetcher.Form>
  );
}
