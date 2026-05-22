# Documentation des helpers de binding

Ce document décrit les utilitaires principaux utilisés pour lier les propriétés du modèle aux composants UI dans OpenPnP : `BeanUtils.bind` (utilisé via `AbstractConfigurationWizard.bind`) et `addWrappedBinding` (défini dans `AbstractConfigurationWizard` et reposant sur `JBindings.bind`).

## `BeanUtils.bind`

Emplacement : `org.openpnp.util.BeanUtils`

Signatures principales :

- `public static AutoBinding bind(UpdateStrategy updateStrategy, Object source, String sourceProperty, Object target, String targetProperty)`
- `public static AutoBinding bind(UpdateStrategy updateStrategy, Object source, String sourceProperty, Object target, String targetProperty, Converter converter)`

Description :
- Crée et démarre (`bind()`) un `AutoBinding` JSR-295 (`org.jdesktop.beansbinding`) entre la propriété `sourceProperty` de l'objet `source` et la propriété `targetProperty` de l'objet `target`.
- Si un `Converter` est fourni, il est appliqué au binding.
- La méthode retourne l'instance `AutoBinding` créée (déjà bindée), que l'appelant peut conserver pour éventuellement `unbind()` plus tard.

Effet secondaire :
- Le binding est créé et immédiatement activé (`binding.bind()`).

Usage typique :

```java
// Lie la propriété model.scanIncrement <-> champ texte 'inc' (conversion longueur)
AutoBinding ab = BeanUtils.bind(UpdateStrategy.READ_WRITE, feeder, "scanIncrement", inc, "text", lengthConverter);
```

Dans `AbstractConfigurationWizard` il existe une méthode d'instance `bind(...)` qui appelle `BeanUtils.bind(...)` puis enregistre le `AutoBinding` dans la liste `autoBindings` pour gestion ultérieure (unbind/dispose).

Fichier de référence : [src/main/java/org/openpnp/util/BeanUtils.java](src/main/java/org/openpnp/util/BeanUtils.java#L1)

---

## `addWrappedBinding`

Emplacement : `org.openpnp.gui.support.AbstractConfigurationWizard`

Signatures exposées :

- `public WrappedBinding addWrappedBinding(Object source, String sourceProperty, Object target, String targetProperty, Converter converter)`
- `public WrappedBinding addWrappedBinding(Object source, String sourceProperty, Object target, String targetProperty)`
- `public WrappedBinding addWrappedBinding(WrappedBinding binding)`

Description générale :
- `addWrappedBinding(...)` fournit un mécanisme de liaison tamponnée (buffered binding) entre une propriété du modèle et un composant UI (généralement un `JComponent` comme `JTextField`).
- L'implémentation s'appuie sur `JBindings.bind(...)` qui crée un objet `JBindings.WrappedBinding`.
- Un `WrappedBinding` crée deux bindings :
  - un binding READ_WRITE entre un `Wrapper` (objet tampon) et la propriété du composant UI (par ex. `JTextField.text`). Ce binding permet d'éditer la valeur dans l'UI sans l'écrire immédiatement dans le modèle.
  - un binding READ du `source` vers le `Wrapper` pour initialiser/mettre à jour le tampon depuis le modèle.
- Le `WrappedBinding` fournit les opérations `save()` (écrire la valeur tampon dans le modèle) et `reset()` (recharger la valeur du modèle dans le tampon).

Comportements additionnels :
- Si le `target` est un `JComponent`, le binding ajoute un `JComponentBackgroundUpdater` qui colore le fond du composant en cas d'échec de conversion (`syncFailed`) pour donner un feedback visuel.
- `AbstractConfigurationWizard.addWrappedBinding(WrappedBinding)` enregistre le binding et lui attache un listener (`ApplyResetBindingListener`) permettant d'activer automatiquement les boutons Apply/Reset de l'UI quand une modification est faite.
- Lors de `setWizardContainer(...)`, `AbstractConfigurationWizard` appelle `createBindings()` puis `loadFromModel()` ; les `WrappedBinding`s sont conservés dans `wrappedBindings` et `loadFromModel()` appelle `reset()` sur chacun pour initialiser l'UI.
- `saveToModel()` appelle `save()` sur chaque `WrappedBinding` pour écrire les valeurs tamponnées dans le modèle.

Usage typique (extrait) :

Voir `CustomFeederConfigurationWizard` :

- Création des champs UI : [src/main/java/org/openpnp/machine/custom/CustomFeederConfigurationWizard.java#L73-L85]
- Bindings :
  ```java
  MutableLocationProxy scanStartLocation = new MutableLocationProxy();
  bind(UpdateStrategy.READ_WRITE, feeder, "scanStartLocation", scanStartLocation, "location");
  addWrappedBinding(scanStartLocation, "lengthX", startX, "text", lengthConverter);
  addWrappedBinding(scanStartLocation, "lengthY", startY, "text", lengthConverter);
  addWrappedBinding(feeder, "address", address, "text");
  addWrappedBinding(feeder, "pitch", pitch, "text", intConverter);
  ```

Fichier(s) de référence :
- `AbstractConfigurationWizard.addWrappedBinding` (implémentation) : [src/main/java/org/openpnp/gui/support/AbstractConfigurationWizard.java](src/main/java/org/openpnp/gui/support/AbstractConfigurationWizard.java#L1)
- `JBindings.bind` et `WrappedBinding` (comportement détaillé) : [src/main/java/org/openpnp/gui/support/JBindings.java](src/main/java/org/openpnp/gui/support/JBindings.java#L1)

---

## Bonnes pratiques

- Utiliser `addWrappedBinding` pour lier des champs éditables afin de bénéficier du tampon, du rollback (`reset`) et du feedback visuel en cas d'erreur de conversion.
- Utiliser `bind` (via `AbstractConfigurationWizard.bind`) pour des liaisons directes simples (par ex. pour des composants non éditables ou des propriétés qui doivent être synchronisées immédiatement).
- Toujours préférer fournir un `Converter` lorsque le type modèle et le type UI diffèrent (ex : longueur / nombre entier).
- Ne pas oublier que `BeanUtils.bind` fait `binding.bind()` immédiatement ; si vous avez besoin de contrôler le moment d'activation, il faut obtenir et gérer l'`AutoBinding` retourné.

---

Si vous voulez, je peux :
- Ajouter des exemples plus complets pour les conversions (`Converter`) utilisés dans le projet (`LengthConverter`, `IntegerConverter`).
- Générer un diagramme simple montrant les flux `model <-> wrapper <-> UI`.
