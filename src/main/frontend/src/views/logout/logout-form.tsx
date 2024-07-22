import { useFetcher } from "react-router-dom";

export default function LogoutForm({ className }: { className?: string }) {
  const fetcher = useFetcher();

  return (
    <fetcher.Form action="/logout" method="post">
      <button className={className}>Logout</button>
    </fetcher.Form>
  );
}
