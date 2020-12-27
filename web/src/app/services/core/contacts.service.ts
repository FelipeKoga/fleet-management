import { Injectable } from "@angular/core";

@Injectable({
  providedIn: "root",
})
export class ContactsService {
  constructor() {}

  public getContacts = () => {
    return [
      {
        name: "Usuário 1",
        update: "30 segundos",
        latitude: -25.414314,
        longitude: -49.9967,
        photo: `https://ui-avatars.com/api/?rounded=true&name=usuario+1&background=6a1b9a&color=fff`,
      },
      {
        name: "Usuário 2",
        update: "15 segundos",
        latitude: -25.5439983,
        longitude: -49.2026901,
        photo: `https://ui-avatars.com/api/?rounded=true&name=usuario+2&background=c51162&color=fff`,
      },
      {
        name: "Usuário 3",
        update: "Desabilitado",
        latitude: -25.0880771,
        longitude: -50.1605277,
        photo: `https://ui-avatars.com/api/?rounded=true&name=usuario+3&background=6a1b9a&color=fff`,
      },
      {
        name: "Usuário 4",
        update: "1 minuto",
        latitude: -25.8635892,
        longitude: -50.4005724,
        photo: `https://ui-avatars.com/api/?rounded=true&name=usuario+4&background=bf360c&color=fff`,
      },
      {
        name: "Usuário 5",
        latitude: -25.415636,
        longitude: -49.996177,
        update: "45 segundos",
        photo: `https://ui-avatars.com/api/?rounded=true&name=usuario+5&background=2c4ebd&color=fff`,
      },
      {
        name: "Usuário 6",
        latitude: -25.414314,
        longitude: -49.9967,
        update: "30",
        photo: `https://ui-avatars.com/api/?rounded=true&name=usuario+1&background=6a1b9a&color=fff`,
      },
      {
        name: "Usuário 7",
        latitude: -25.5439983,
        longitude: -49.2026901,
        update: "30",
        photo: `https://ui-avatars.com/api/?rounded=true&name=usuario+2&background=c51162&color=fff`,
      },
      {
        name: "Usuário 8",
        latitude: -25.422301,
        longitude: -49.993794,
        update: "30",
        photo: `https://ui-avatars.com/api/?rounded=true&name=usuario+3&background=6a1b9a&color=fff`,
      },
      {
        name: "Usuário 9",
        latitude: -25.418911,
        longitude: -49.99513,
        update: "30",
        photo: `https://ui-avatars.com/api/?rounded=true&name=usuario+4&background=bf360c&color=fff`,
      },
      {
        name: "Usuário 10",
        latitude: -25.419861,
        longitude: -49.998441,
        update: "30",
        photo: `https://ui-avatars.com/api/?rounded=true&name=usuario+12&background=bb4ebd&color=fff`,
      },
    ];
  };
}
