import { useFetcher } from "react-router-dom";

export default function LogoutForm() {
  const fetcher = useFetcher();

  return (
    <fetcher.Form action="/logout" method="post">
      <button className="rounded bg-cyan-200 p-4">Logout</button>
    </fetcher.Form>
  );
}
