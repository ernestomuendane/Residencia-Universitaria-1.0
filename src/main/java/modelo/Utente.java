package modelo;

public abstract class Utente {

    private final String nome;
    private String telefone;
    private String email;

    protected  Utente(String nome, String telefone, String email){
        if(nome == null || nome.isBlank()){
            throw new IllegalArgumentException(" O nome é obrigatorio. ");
        }
        this.nome = nome;
        setTelefone(telefone);
        setEmail(email);
    }

    public abstract int getId();

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null && !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido: " + email);
        }
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getId() == ((Utente) o).getId();
    }

    @Override
    public int hashCode() {
        return 31 * getClass().hashCode() + getId();
    }

    @Override
    public String toString() {
        return "Utente{" +
                "nome='" + nome + '\'' +
                ", telefone='" + telefone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
