import { useFetcher } from "react-router-dom";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import TextInput from "../../components/form/text-input.tsx";

const schema = z.object({
  name: z.string().min(2).max(64),
  email: z.string().email(),
});

type Schema = z.infer<typeof schema>;

export default function RegistrationForm({ action }: { action: string }) {
  const fetcher = useFetcher();
  const { register, handleSubmit, formState } = useForm<Schema>({ resolver: zodResolver(schema) });

  function onSubmit(data: Schema) {
    fetcher.submit(data, { method: "post", action, encType: "application/json" });
  }

  return (
    <fetcher.Form onSubmit={handleSubmit(onSubmit)} className="flex w-full flex-col gap-2">
      <TextInput register={register("name")} error={formState.errors.name?.message} placeholder="John Smith" />
      <TextInput register={register("email")} error={formState.errors.email?.message} placeholder="name@example.com" />
      <button tabIndex={0} disabled={fetcher.state !== "idle"} className="btn btn-primary mt-2">
        Register
      </button>
    </fetcher.Form>
  );
}
