import numpy as np
import pandas as pd
from sklearn.base import BaseEstimator, ClassifierMixin
from sklearn.metrics import accuracy_score, confusion_matrix, classification_report
import logging

from sklearn.model_selection import train_test_split

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

class CustomNaiveBayes(BaseEstimator, ClassifierMixin):
    def __init__(self, laplace_correction=False):
        self.laplace_correction = laplace_correction

    def fit(self, X, y):
        """Uczenie klasyfikatora Naive Bayes."""
        self.classes_ = np.unique(y)
        self.prior_probabilities_ = {cls: np.mean(y == cls) for cls in self.classes_}

        self.conditional_probabilities_ = {}
        for cls in self.classes_:
            cls_data = X[y == cls]
            self.conditional_probabilities_[cls] = []
            for i in range(X.shape[1]):
                counts = np.bincount(cls_data[:, i], minlength=X[:, i].max() + 1).astype(float)
                if self.laplace_correction:
                    counts += 1
                self.conditional_probabilities_[cls].append(counts / counts.sum())

        return self

    def predict_proba(self, X):
        probabilities = []
        for x in X:
            probs = []
            for cls in self.classes_:
                prob = np.log(self.prior_probabilities_[cls])  # Log prior probability
                for i, value_idx in enumerate(x):
                    # Add Laplace correction to probabilities
                    conditional_prob = self.conditional_probabilities_[cls][i][value_idx]
                    prob += np.log(conditional_prob + 1e-10)  # Stabilize log
                probs.append(prob)
            probabilities.append(probs)

        probabilities = np.exp(probabilities)  # Convert log probabilities back
        probabilities /= probabilities.sum(axis=1, keepdims=True)  # Normalize
        return probabilities

    def predict(self, X):
        """Predykcja klasy dla każdego X."""
        return self.classes_[np.argmax(self.predict_proba(X), axis=1)]


def discretize(data, bins):
    """
    Dyskretyzacja danych przy użyciu zdefiniowanych granic.

    Parametry:
    - data: dane wejściowe (1D array)
    - bins: granice przedziałów

    Zwraca:
    - zdyskretyzowane dane jako indeksy przedziałów
    """
    indices = np.digitize(data, bins, right=False) - 1
    return np.clip(indices, 0, len(bins) - 2)


def wine():
    wine_data = np.genfromtxt('wine.data', delimiter=',')
    X = wine_data[:, 1:]
    y = wine_data[:, 0].astype(int)

    buckets = [3, 5, 7]
    laplace_options = [False, True]
    tests_number = 100

    for n_bins in buckets:
        for laplace in laplace_options:
            logging.info(f"Trening klasyfikatora: Buckets={n_bins}, Laplace={laplace}")
            mean_accuracy = 0

            for _ in range(tests_number):
                X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=None)

                bins = [np.linspace(np.min(X_train[:, i]), np.max(X_train[:, i]), n_bins + 1) for i in range(X.shape[1])]
                X_train_discretized = np.array([
                    np.clip(np.digitize(X_train[:, i], bins=bins[i], right=False) - 1, 0, n_bins - 1)
                    for i in range(X.shape[1])
                ]).T

                X_test_discretized = np.array([
                    np.clip(np.digitize(X_test[:, i], bins=bins[i], right=False) - 1, 0, n_bins - 1)
                    for i in range(X.shape[1])
                ]).T

                nbc = CustomNaiveBayes(laplace_correction=laplace)
                nbc.fit(X_train_discretized, y_train)

                y_pred = nbc.predict(X_test_discretized)
                mean_accuracy += accuracy_score(y_test, y_pred)

            mean_accuracy /= tests_number

            X_discretized = np.array([
                np.clip(np.digitize(X[:, i], bins=bins[i], right=False) - 1, 0, n_bins - 1)
                for i in range(X.shape[1])
            ]).T

            print(f"\n{'=' * 40}")
            print(f"Buckets: {n_bins}, Laplace: {laplace}")
            print(f"Mean Accuracy: {mean_accuracy:.4f}")
            nbc.fit(X_discretized, y)
            y_pred_full = nbc.predict(X_discretized)
            conf_matrix = confusion_matrix(y, y_pred_full)
            class_report = classification_report(y, y_pred_full, zero_division=0)
            print("Confusion Matrix:")
            print(conf_matrix)
            print("Classification Report:")
            print(class_report)
            print(f"{'=' * 40}")

def lenses():
    file_path = "lenses.data"
    columns = ["id", "age", "spectacle-prescrip", "astigmatism", "tear-prod-rate", "class"]

    logging.info("Wczytywanie danych...")
    data = pd.read_csv(file_path, delim_whitespace=True, names=columns).drop(columns=["id"])
    data["class"] -= 1

    X = data.iloc[:, :-1].values
    y = data.iloc[:, -1].values

    for laplace in [False, True]:
        logging.info(f"Trening klasyfikatora: Laplace={laplace}")
        nbc = CustomNaiveBayes(laplace_correction=laplace)
        nbc.fit(X, y)
        y_pred = nbc.predict(X)

        accuracy = accuracy_score(y, y_pred)
        conf_matrix = confusion_matrix(y, y_pred)
        class_report = classification_report(y, y_pred, zero_division=0)

        print(f"\n{'=' * 40}")
        print(f"Laplace Correction: {laplace}")
        print(f"Accuracy: {accuracy:.4f}")
        print("Confusion Matrix:")
        print(conf_matrix)
        print("Classification Report:")
        print(class_report)
        print(f"{'=' * 40}")

if __name__ == "__main__":
    lenses()
    # wine()
