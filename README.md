# artifact

[Artifact](https://playartifact.com) deck codec.

## Usage

### Encoder

```Clojure
user=> (require '[artifact.deck-encoder])
nil
user=> (artifact.deck-encoder/encode {:heroes [{:id 4005, :turn 2} {:id 10014, :turn 1} {:id 10017, :turn 3} {:id 10026, :turn 1} {:id 10047, :turn 1}], :cards [{:id 3000, :count 2} {:id 3001, :count 1} {:id 10091, :count 3} {:id 10102, :count 3} {:id 10128, :count 3} {:id 10165, :count 3} {:id 10168, :count 3} {:id 10169, :count 3} {:id 10185, :count 3} {:id 10223, :count 1} {:id 10234, :count 3} {:id 10260, :count 1} {:id 10263, :count 1} {:id 10322, :count 3} {:id 10354, :count 3}], :name "Green/Black Example"})
"ADCJWkTZX05uwGDCRV4XQGy3QGLmqUBg4GQJgGLGgO7AaABR3JlZW4vQmxhY2sgRXhhbXBsZQ__"
```

```Bash
$ clj -m artifact.deck-encoder "{:heroes [{:id 4005, :turn 2} {:id 10014, :turn 1} {:id 10017, :turn 3} {:id 10026, :turn 1} {:id 10047, :turn 1}], :cards [{:id 3000, :count 2} {:id 3001, :count 1} {:id 10091, :count 3} {:id 10102, :count 3} {:id 10128, :count 3} {:id 10165, :count 3} {:id 10168, :count 3} {:id 10169, :count 3} {:id 10185, :count 3} {:id 10223, :count 1} {:id 10234, :count 3} {:id 10260, :count 1} {:id 10263, :count 1} {:id 10322, :count 3} {:id 10354, :count 3}], :name \"Green/Black Example\"}"
"ADCJWkTZX05uwGDCRV4XQGy3QGLmqUBg4GQJgGLGgO7AaABR3JlZW4vQmxhY2sgRXhhbXBsZQ__"
```

### Decoder

```Clojure
user=> (require '[artifact.deck-decoder])
nil
user=> (artifact.deck-decoder/decode "ADCJWkTZX05uwGDCRV4XQGy3QGLmqUBg4GQJgGLGgO7AaABR3JlZW4vQmxhY2sgRXhhbXBsZQ__")
{:heroes [{:id 4005, :turn 2} {:id 10014, :turn 1} {:id 10017, :turn 3} {:id 10026, :turn 1} {:id 10047, :turn 1}], :cards [{:id 3000, :count 2} {:id 3001, :count 1} {:id 10091, :count 3} {:id 10102, :count 3} {:id 10128, :count 3} {:id 10165, :count 3} {:id 10168, :count 3} {:id 10169, :count 3} {:id 10185, :count 3} {:id 10223, :count 1} {:id 10234, :count 3} {:id 10260, :count 1} {:id 10263, :count 1} {:id 10322, :count 3} {:id 10354, :count 3}], :name "Green/Black Example"}
```

```Bash
$ clj -m artifact.deck-decoder "ADCJWkTZX05uwGDCRV4XQGy3QGLmqUBg4GQJgGLGgO7AaABR3JlZW4vQmxhY2sgRXhhbXBsZQ__"
{:heroes
 [{:id 4005, :turn 2}
  {:id 10014, :turn 1}
  {:id 10017, :turn 3}
  {:id 10026, :turn 1}
  {:id 10047, :turn 1}],
 :cards
 [{:id 3000, :count 2}
  {:id 3001, :count 1}
  {:id 10091, :count 3}
  {:id 10102, :count 3}
  {:id 10128, :count 3}
  {:id 10165, :count 3}
  {:id 10168, :count 3}
  {:id 10169, :count 3}
  {:id 10185, :count 3}
  {:id 10223, :count 1}
  {:id 10234, :count 3}
  {:id 10260, :count 1}
  {:id 10263, :count 1}
  {:id 10322, :count 3}
  {:id 10354, :count 3}],
 :name "Green/Black Example"}
```
